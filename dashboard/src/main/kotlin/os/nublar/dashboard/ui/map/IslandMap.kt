package os.nublar.dashboard.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.material.Text
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import os.nublar.designsystem.NublarColors

/** Loads a bundled PNG resource into an ImageBitmap. */
/** Selection highlight colors: green = editable (edit mode on), yellow = locked (edit mode off). */
private val SELECT_EDITABLE = Color(0xFF54D875)
private val SELECT_LOCKED = Color(0xFFD5CD58)

private fun loadBitmap(resource: String): ImageBitmap {
    val bytes = object {}.javaClass.classLoader.getResourceAsStream(resource)!!.use { it.readBytes() }
    return org.jetbrains.skia.Image.makeFromEncoded(bytes).toComposeImageBitmap()
}

/** All raster assets drawn by [IslandMap], loaded together on a background thread. */
private data class MapBitmaps(
    val island: ImageBitmap,
    val skull: ImageBitmap,
    val bronto: ImageBitmap,
    val raptor: ImageBitmap,
    val tyrannosaurus: ImageBitmap,
    val triceratops: ImageBitmap,
    val gallimimus: ImageBitmap,
    val segisaurus: ImageBitmap,
    val metriacanthosaurus: ImageBitmap,
    val parasaurolophus: ImageBitmap,
    val proceratosaurus: ImageBitmap,
    val herrerasaurus: ImageBitmap,
    val dilophosaurus: ImageBitmap,
    val helipad: ImageBitmap,
    val visitorCenter: ImageBitmap,
    val dock: ImageBitmap,
)

/**
 * Loads the map's raster assets on [Dispatchers.IO] so the large island PNG
 * and species icons don't decode on the UI thread during first composition.
 * Returns `null` until loading completes.
 */
@Composable
private fun rememberMapBitmaps(): MapBitmaps? {
    var bitmaps by remember { mutableStateOf<MapBitmaps?>(null) }
    LaunchedEffect(Unit) {
        bitmaps = withContext(Dispatchers.IO) {
            MapBitmaps(
                island = loadBitmap("island.png"),
                skull = loadBitmap("icons/dino-skull.png"),
                bronto = loadBitmap("icons/dino/brontosaurus.png"),
                raptor = loadBitmap("icons/dino/raptor.png"),
                tyrannosaurus = loadBitmap("icons/dino/tyrannosaurus.png"),
                triceratops = loadBitmap("icons/dino/triceratops.png"),
                gallimimus = loadBitmap("icons/dino/gallimimus.png"),
                segisaurus = loadBitmap("icons/dino/segisaurus.png"),
                metriacanthosaurus = loadBitmap("icons/dino/metriacanthosaurus.png"),
                parasaurolophus = loadBitmap("icons/dino/parasaurolophus.png"),
                proceratosaurus = loadBitmap("icons/dino/proceratosaurus.png"),
                herrerasaurus = loadBitmap("icons/dino/herrerasaurus.png"),
                dilophosaurus = loadBitmap("icons/dino/dilophosaurus.png"),
                helipad = loadBitmap("icons/facility-helipad.png"),
                visitorCenter = loadBitmap("icons/facility-visitor-center.png"),
                dock = loadBitmap("icons/facility-dock.png"),
            )
        }
    }
    return bitmaps
}

/**
 * Isla Nublar map component: the island artwork as a base layer, plus
 * independently toggleable overlays (fences/paddocks, dinosaurs, tour
 * vehicles, staff), each with a hover tooltip. Isolated here so it can be
 * embedded in a real screen (see IslandMapView) or driven standalone in
 * MapPreviewMain for debugging.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IslandMap(
    modifier: Modifier = Modifier,
    activeLayers: Set<MapLayer> = MapLayer.entries.toSet(),
    // Pure UI component: all layer data comes from the caller (screens feed it
    // from their ViewModel, which reads a repository) — no data loading here.
    facilities: List<FacilityMarker> = emptyList(),
    dinosaurs: List<DinosaurMarker> = emptyList(),
    vehicles: List<VehicleMarker> = emptyList(),
    staff: List<StaffMarker> = emptyList(),
    paddockShapes: List<PaddockShape> = emptyList(),
    // Current map zoom (1f = unzoomed). Paddock markers dampen against this so
    // they grow sub-linearly with the map instead of ballooning 1:1 — see
    // drawPaddockShape. Left at 1f by the fixed-size screens (no zoom there).
    zoom: Float = 1f,
    selectedPaddockId: String? = null,
    selectedVertexIndex: Int? = null,
    selectedFacilityId: String? = null,
    editMode: Boolean = false,
    onPaddockSelected: (String?) -> Unit = {},
    onVertexSelected: (Int?) -> Unit = {},
    onVertexMoved: (paddockId: String, vertexIndex: Int, newPos: FractionalPoint) -> Unit = { _, _, _ -> },
    onFacilitySelected: (String?) -> Unit = {},
    onFacilityMoved: (facilityId: String, newPos: FractionalPoint) -> Unit = { _, _ -> },
) {
    val bitmaps = rememberMapBitmaps()
    if (bitmaps == null) return // assets loading on Dispatchers.IO
    val textMeasurer = rememberTextMeasurer()

    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        // Per-paddock fence visuals, driven by the armed/unarmed status. A
        // paddock going armed -> unarmed flashes its fence orange, then fades it
        // out; one loaded already unarmed is simply hidden (no animation). The
        // Canvas below reads these values, so updates re-draw the fences.
        val fenceVisuals = remember { mutableStateMapOf<String, FenceVisual>() }
        val prevArmed = remember { mutableStateMapOf<String, Boolean>() }
        paddockShapes.forEach { shape ->
            key(shape.id) {
                LaunchedEffect(shape.armed) {
                    val wasArmed = prevArmed[shape.id]
                    prevArmed[shape.id] = shape.armed
                    when {
                        shape.armed -> fenceVisuals[shape.id] = FenceVisual(alpha = 1f, flash = 0f)
                        wasArmed == true -> {
                            // armed -> unarmed: flash the fence orange a few
                            // times, then fade it away.
                            repeat(3) {
                                animate(0f, 1f, animationSpec = tween(110)) { v, _ ->
                                    fenceVisuals[shape.id] = FenceVisual(alpha = 1f, flash = v)
                                }
                                animate(1f, 0f, animationSpec = tween(110)) { v, _ ->
                                    fenceVisuals[shape.id] = FenceVisual(alpha = 1f, flash = v)
                                }
                            }
                            animate(1f, 0f, animationSpec = tween(450)) { v, _ ->
                                fenceVisuals[shape.id] = FenceVisual(alpha = v, flash = 0f)
                            }
                            fenceVisuals[shape.id] = FenceVisual(alpha = 0f, flash = 0f)
                        }
                        // Loaded already unarmed (or first composition): hidden.
                        else -> fenceVisuals[shape.id] = FenceVisual(alpha = 0f, flash = 0f)
                    }
                }
            }
        }

        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            fun pt(p: FractionalPoint) = Offset(w * p.x, h * p.y)

            drawImage(bitmaps.island, dstSize = IntSize(w.toInt(), h.toInt()))

            if (MapLayer.Dinosaurs in activeLayers) {
                dinosaurs.forEach { marker -> drawDinosaur(marker, pt(marker.position), w) }
            }
            if (MapLayer.Vehicles in activeLayers) {
                vehicles.forEach { marker -> drawVehicle(marker, pt(marker.position), w) }
            }
            if (MapLayer.Staff in activeLayers) {
                staff.forEach { marker -> drawStaff(marker, pt(marker.position), w) }
            }
            // Draw order (bottom to top): paddock fences, then facility icons on
            // top of the fences, then paddock species icons/labels/handles last
            // so they sit above everything and stay easy to see and select.
            // Within each paddock pass, paddocks are stacked by priority so the
            // most relevant sit on top: a currently-failing fence (mid disarm
            // animation) rises above the rest, and the selected paddock above
            // that. sortedBy is stable, so equal-priority paddocks keep order.
            val orderedPaddocks = paddockShapes.sortedBy { shape ->
                val visible = (fenceVisuals[shape.id]?.alpha ?: if (shape.armed) 1f else 0f) > 0f
                val failing = !shape.armed && !shape.isBuilding && visible
                when {
                    shape.id == selectedPaddockId -> 2
                    failing -> 1
                    else -> 0
                }
            }

            if (MapLayer.Paddocks in activeLayers) {
                orderedPaddocks.forEach { shape ->
                    val fv = fenceVisuals[shape.id]
                        ?: FenceVisual(alpha = if (shape.armed) 1f else 0f, flash = 0f)
                    drawPaddockFence(
                        shape, ::pt, w,
                        selected = shape.id == selectedPaddockId,
                        editMode = editMode,
                        alpha = fv.alpha,
                        flash = fv.flash,
                    )
                }
            }
            // Facilities drawn AFTER paddock fences so their icons sit on top of
            // the fence lines (but below paddock species icons/labels).
            if (MapLayer.Facilities in activeLayers) {
                facilities.forEach { marker ->
                    drawFacility(
                        marker, pt(marker.position), w, bitmaps.helipad, bitmaps.visitorCenter, bitmaps.dock,
                        selected = marker.id == selectedFacilityId,
                        editMode = editMode,
                    )
                }
            }
            if (MapLayer.Paddocks in activeLayers) {
                orderedPaddocks.forEach { shape ->
                    val isSel = shape.id == selectedPaddockId
                    val fenceAlpha = fenceVisuals[shape.id]?.alpha ?: if (shape.armed) 1f else 0f
                    drawPaddockMarker(
                        shape, ::pt, w, textMeasurer,
                        speciesIcons(
                            shape, bitmaps.skull, bitmaps.bronto, bitmaps.raptor,
                            bitmaps.tyrannosaurus, bitmaps.triceratops, bitmaps.gallimimus, bitmaps.segisaurus,
                            bitmaps.metriacanthosaurus, bitmaps.parasaurolophus, bitmaps.proceratosaurus,
                            bitmaps.herrerasaurus, bitmaps.dilophosaurus,
                        ),
                        selected = isSel,
                        editMode = editMode,
                        selectedVertexIndex = if (isSel) selectedVertexIndex else null,
                        zoom = zoom,
                        fenceAlpha = fenceAlpha,
                    )
                }
            }
        }

        // Interaction overlay: paddock/facility selection (tap), vertex
        // selection (tap a dot), vertex + facility dragging, and arrow-key nudging.
        val paddocksInteractive = MapLayer.Paddocks in activeLayers && paddockShapes.isNotEmpty()
        val facilitiesInteractive = MapLayer.Facilities in activeLayers && facilities.isNotEmpty()

        // Tooltips must be CHILDREN of the interaction overlay, not siblings
        // above it: Compose routes pointer events to the topmost sibling only,
        // so a tooltip hit-target sitting over a facility disc would swallow
        // clicks (dead zone at the marker center). As a descendant, the tooltip
        // gets hover events while the overlay (an ancestor in the dispatch
        // chain) still sees every press for selection/dragging.
        val tooltips: @Composable () -> Unit = {
            if (MapLayer.Facilities in activeLayers) {
                facilities.forEach { marker ->
                    MarkerTooltip(position = marker.position, text = marker.label, widthPx = widthPx, heightPx = heightPx)
                }
            }
            if (MapLayer.Dinosaurs in activeLayers) {
                dinosaurs.forEach { marker ->
                    val text = buildString {
                        append(marker.label ?: marker.species.displayName)
                        if (marker.confidence != "high") append(" (confidence: ${marker.confidence})")
                    }
                    MarkerTooltip(position = marker.position, text = text, widthPx = widthPx, heightPx = heightPx)
                }
            }
            if (MapLayer.Vehicles in activeLayers) {
                vehicles.forEach { marker ->
                    MarkerTooltip(
                        position = marker.position,
                        text = "Vehicle ${marker.id}",
                        widthPx = widthPx,
                        heightPx = heightPx,
                    )
                }
            }
            if (MapLayer.Staff in activeLayers) {
                staff.forEach { marker ->
                    MarkerTooltip(position = marker.position, text = marker.id, widthPx = widthPx, heightPx = heightPx)
                }
            }
        }

        if (paddocksInteractive || facilitiesInteractive) {
            MapInteractionOverlay(
                paddockShapes = if (paddocksInteractive) paddockShapes else emptyList(),
                selectedPaddockId = selectedPaddockId,
                selectedVertexIndex = selectedVertexIndex,
                facilities = if (facilitiesInteractive) facilities else emptyList(),
                selectedFacilityId = selectedFacilityId,
                editMode = editMode,
                widthPx = widthPx,
                heightPx = heightPx,
                onPaddockSelected = onPaddockSelected,
                onVertexSelected = onVertexSelected,
                onVertexMoved = onVertexMoved,
                onFacilitySelected = onFacilitySelected,
                onFacilityMoved = onFacilityMoved,
            ) {
                tooltips()
            }
        } else {
            tooltips()
        }
    }
}

/**
 * Transparent overlay over the map that handles marker interaction:
 * tapping a paddock or facility selects it; in [editMode], tapping a vertex
 * handle selects that dot (arrow keys then nudge it slowly), dragging a handle
 * moves it, and dragging a selected facility moves the whole facility. All
 * coordinates convert between pixels and canvas-fractions via the map's pixel
 * size, so edits map 1:1 to the stored fractional coordinates.
 */
@Composable
private fun MapInteractionOverlay(
    paddockShapes: List<PaddockShape>,
    selectedPaddockId: String?,
    selectedVertexIndex: Int?,
    facilities: List<FacilityMarker>,
    selectedFacilityId: String?,
    editMode: Boolean,
    widthPx: Float,
    heightPx: Float,
    onPaddockSelected: (String?) -> Unit,
    onVertexSelected: (Int?) -> Unit,
    onVertexMoved: (paddockId: String, vertexIndex: Int, newPos: FractionalPoint) -> Unit,
    onFacilitySelected: (String?) -> Unit,
    onFacilityMoved: (facilityId: String, newPos: FractionalPoint) -> Unit,
    // Rendered INSIDE the overlay's box (e.g. hover tooltips) so pointer
    // events still reach the overlay's gestures — see the caller's comment.
    content: @Composable () -> Unit = {},
) {
    // Pixel size must ALSO be read live (not captured): the preview's zoom
    // buttons resize the map, and the pointer-input coroutine (keyed on Unit)
    // would otherwise keep converting positions with the stale size, shifting
    // every hit-test after a zoom.
    val widthPxState = rememberUpdatedState(widthPx)
    val heightPxState = rememberUpdatedState(heightPx)
    fun toFraction(offset: Offset) =
        FractionalPoint(offset.x / widthPxState.value, offset.y / heightPxState.value)

    // Read live shapes/selection/callbacks from gesture & key lambdas WITHOUT
    // keying pointerInput on them — otherwise every vertex move (which changes
    // paddockShapes) restarts the pointer-input block and cancels the drag.
    val shapesState = rememberUpdatedState(paddockShapes)
    val selectedState = rememberUpdatedState(selectedPaddockId)
    val selectedVertexState = rememberUpdatedState(selectedVertexIndex)
    val onSelectedState = rememberUpdatedState(onPaddockSelected)
    val onVertexSelectedState = rememberUpdatedState(onVertexSelected)
    val onMovedState = rememberUpdatedState(onVertexMoved)
    val facilitiesState = rememberUpdatedState(facilities)
    val selectedFacilityState = rememberUpdatedState(selectedFacilityId)
    val editModeState = rememberUpdatedState(editMode)
    val onFacilitySelectedState = rememberUpdatedState(onFacilitySelected)
    val onFacilityMovedState = rememberUpdatedState(onFacilityMoved)

    // Facility hit radius in canvas fractions (slightly larger than the disc).
    val facilityHitRadius = 0.028f

    val focusRequester = remember { FocusRequester() }
    // Any selection (paddock, vertex, or facility) needs focus in edit mode so
    // arrow-key nudge events arrive.
    LaunchedEffect(selectedPaddockId, selectedVertexIndex, selectedFacilityId, editMode) {
        if (editMode && (selectedPaddockId != null || selectedVertexIndex != null || selectedFacilityId != null)) {
            focusRequester.requestFocus()
        }
    }

    // Arrow-key nudge step: 1 canvas pixel, so movement is slow/precise (key
    // auto-repeat while held moves it continuously). Holding shift moves 10x
    // for coarse repositioning.
    val stepX = 1f / widthPx
    val stepY = 1f / heightPx
    val shiftMultiplier = 10f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                if (!editModeState.value) return@onKeyEvent false   // no nudging when locked
                val scale = if (event.isShiftPressed) shiftMultiplier else 1f
                val stepX = stepX * scale
                val stepY = stepY * scale
                val (dx, dy) = when (event.key) {
                    Key.DirectionLeft -> -stepX to 0f
                    Key.DirectionRight -> stepX to 0f
                    Key.DirectionUp -> 0f to -stepY
                    Key.DirectionDown -> 0f to stepY
                    else -> return@onKeyEvent false
                }
                // A selected facility nudges the whole marker.
                val facilityId = selectedFacilityState.value
                if (facilityId != null) {
                    val f = facilitiesState.value.firstOrNull { it.id == facilityId } ?: return@onKeyEvent false
                    onFacilityMovedState.value(
                        facilityId,
                        FractionalPoint(
                            (f.position.x + dx).coerceIn(0f, 1f),
                            (f.position.y + dy).coerceIn(0f, 1f),
                        ),
                    )
                    return@onKeyEvent true
                }
                // Otherwise nudge the selected paddock: a selected node moves
                // just that vertex; with no node selected, the WHOLE paddock
                // moves rigidly (every vertex by the same delta).
                val id = selectedState.value ?: return@onKeyEvent false
                val shape = shapesState.value.firstOrNull { it.id == id } ?: return@onKeyEvent false
                val vi = selectedVertexState.value
                if (vi == null) {
                    // Clamp the delta (not each vertex) so the outline can't
                    // be squashed against the canvas edge — it stops rigid.
                    val loX = -shape.vertices.minOf { it.x }
                    val hiX = 1f - shape.vertices.maxOf { it.x }
                    val loY = -shape.vertices.minOf { it.y }
                    val hiY = 1f - shape.vertices.maxOf { it.y }
                    val ddx = if (loX <= hiX) dx.coerceIn(loX, hiX) else 0f
                    val ddy = if (loY <= hiY) dy.coerceIn(loY, hiY) else 0f
                    if (ddx != 0f || ddy != 0f) {
                        shape.vertices.forEachIndexed { i, v ->
                            onMovedState.value(id, i, FractionalPoint(v.x + ddx, v.y + ddy))
                        }
                    }
                    return@onKeyEvent true
                }
                val v = shape.vertices.getOrNull(vi) ?: return@onKeyEvent false
                onMovedState.value(
                    id, vi,
                    FractionalPoint((v.x + dx).coerceIn(0f, 1f), (v.y + dy).coerceIn(0f, 1f)),
                )
                true
            }
            // Single unified gesture handler so a plain click reliably selects
            // (a facility, a paddock, or — in edit mode — a vertex node) without
            // having to first drag. A press that then moves past touch slop turns
            // into a drag of the grabbed facility/vertex (edit mode only).
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val startFrac = toFraction(down.position)

                    // Resolve what's under the pointer at press time.
                    val facility = facilitiesState.value.nearestWithin(startFrac, facilityHitRadius)
                    val selShape = shapesState.value.firstOrNull { it.id == selectedState.value }
                    val vertexHit = if (editModeState.value && selShape != null && !selShape.isBuilding) {
                        selShape.nearestVertexWithin(startFrac, 18f / widthPxState.value)
                    } else {
                        null
                    }
                    // A building has no fence nodes — it's grabbed by its icon.
                    val buildingHit = if (facility == null) {
                        shapesState.value.buildingIconWithin(startFrac, facilityHitRadius)
                    } else {
                        null
                    }

                    // In edit mode a grabbed facility, vertex, or building icon can be dragged.
                    val draggable = editModeState.value &&
                        (facility != null || vertexHit != null || buildingHit != null)
                    if (draggable) {
                        val slop = awaitTouchSlopOrCancellation(down.id) { change, _ -> change.consume() }
                        if (slop != null) {
                            // Became a drag: select the grabbed item, then follow.
                            if (facility != null) {
                                onFacilitySelectedState.value(facility.id)
                                onSelectedState.value(null)
                                onVertexSelectedState.value(null)
                            } else if (vertexHit != null) {
                                onVertexSelectedState.value(vertexHit)
                            } else if (buildingHit != null) {
                                onSelectedState.value(buildingHit.id)
                                onVertexSelectedState.value(null)
                                onFacilitySelectedState.value(null)
                            }
                            fun apply(pos: Offset) {
                                val f = toFraction(pos)
                                when {
                                    facility != null -> onFacilityMovedState.value(
                                        facility.id,
                                        FractionalPoint(f.x.coerceIn(0f, 1f), f.y.coerceIn(0f, 1f)),
                                    )
                                    vertexHit != null && selShape != null ->
                                        onMovedState.value(selShape.id, vertexHit, f)
                                    // Dragging a building moves its anchor point.
                                    buildingHit != null -> onMovedState.value(
                                        buildingHit.id, 0,
                                        FractionalPoint(f.x.coerceIn(0f, 1f), f.y.coerceIn(0f, 1f)),
                                    )
                                }
                            }
                            apply(slop.position)
                            drag(down.id) { change ->
                                change.consume()
                                apply(change.position)
                            }
                            return@awaitEachGesture
                        }
                    }

                    // Not a drag → a click: select by hit priority.
                    if (waitForUpOrCancellation() == null) return@awaitEachGesture
                    when {
                        // Vertex node (only non-null in edit mode with a paddock selected).
                        vertexHit != null -> {
                            onVertexSelectedState.value(vertexHit)
                            focusRequester.requestFocus()
                        }
                        // Facility disc — clears any paddock/vertex selection.
                        facility != null -> {
                            onFacilitySelectedState.value(facility.id)
                            onSelectedState.value(null)
                            onVertexSelectedState.value(null)
                        }
                        // A building's icon is its whole hit-target.
                        buildingHit != null -> {
                            onSelectedState.value(buildingHit.id)
                            onVertexSelectedState.value(null)
                            onFacilitySelectedState.value(null)
                        }
                        // Otherwise (re)select whichever fenced paddock contains the point.
                        else -> {
                            val hit = shapesState.value.lastOrNull {
                                !it.isBuilding && pointInPolygon(startFrac, it.toFractionalPoints())
                            }
                            onSelectedState.value(hit?.id)
                            onVertexSelectedState.value(null)
                            onFacilitySelectedState.value(null)
                        }
                    }
                }
            },
    ) {
        content()
    }
}

/** Anchor point of a building paddock (its single stored vertex / centroid). */
private fun PaddockShape.anchor(): FractionalPoint? {
    val pts = toFractionalPoints()
    if (pts.isEmpty()) return null
    return FractionalPoint(pts.map { it.x }.average().toFloat(), pts.map { it.y }.average().toFloat())
}

/**
 * The building paddock whose icon is within [radiusFrac] of [frac], or null.
 * Buildings have no traced outline, so they're hit by their icon rather than
 * by [pointInPolygon] (which a single-point shape could never satisfy).
 */
private fun List<PaddockShape>.buildingIconWithin(frac: FractionalPoint, radiusFrac: Float): PaddockShape? {
    var best: PaddockShape? = null
    var bestSq = radiusFrac * radiusFrac
    for (shape in this) {
        if (!shape.isBuilding) continue
        val a = shape.anchor() ?: continue
        val dx = a.x - frac.x
        val dy = a.y - frac.y
        val d = dx * dx + dy * dy
        if (d <= bestSq) {
            bestSq = d
            best = shape
        }
    }
    return best
}

/** The facility whose position is within [radiusFrac] of [frac] (nearest if several), or null. */
private fun List<FacilityMarker>.nearestWithin(frac: FractionalPoint, radiusFrac: Float): FacilityMarker? {
    val thresholdSq = radiusFrac * radiusFrac
    var best: FacilityMarker? = null
    var bestSq = thresholdSq
    for (f in this) {
        val dx = f.position.x - frac.x
        val dy = f.position.y - frac.y
        val d = dx * dx + dy * dy
        if (d <= bestSq) {
            bestSq = d
            best = f
        }
    }
    return best
}

/** Index of the vertex within [radiusFrac] of [frac], or null. */
private fun PaddockShape.nearestVertexWithin(frac: FractionalPoint, radiusFrac: Float): Int? {
    val thresholdSq = radiusFrac * radiusFrac
    val idx = vertices.indexOfMinByOrNull { v ->
        val dx = v.x - frac.x
        val dy = v.y - frac.y
        dx * dx + dy * dy
    } ?: return null
    val v = vertices[idx]
    val dx = v.x - frac.x
    val dy = v.y - frac.y
    return if (dx * dx + dy * dy <= thresholdSq) idx else null
}

/** Ray-casting point-in-polygon test in fractional coordinates. */
private fun pointInPolygon(p: FractionalPoint, polygon: List<FractionalPoint>): Boolean {
    if (polygon.size < 3) return false
    var inside = false
    var j = polygon.size - 1
    for (i in polygon.indices) {
        val a = polygon[i]
        val b = polygon[j]
        if ((a.y > p.y) != (b.y > p.y) &&
            p.x < (b.x - a.x) * (p.y - a.y) / (b.y - a.y) + a.x
        ) {
            inside = !inside
        }
        j = i
    }
    return inside
}

private inline fun <T> List<T>.indexOfMinByOrNull(selector: (T) -> Float): Int? {
    if (isEmpty()) return null
    var bestIdx = 0
    var bestVal = selector(this[0])
    for (i in 1 until size) {
        val v = selector(this[i])
        if (v < bestVal) {
            bestVal = v
            bestIdx = i
        }
    }
    return bestIdx
}

/** Selection-highlight color for a paddock's outline/label/handles. */
private fun paddockLineColor(selected: Boolean, editMode: Boolean): Color = when {
    selected && editMode -> SELECT_EDITABLE   // editable
    selected -> SELECT_LOCKED                 // locked (selected, edit off)
    else -> Color(0xFFE2E0BF)                 // default cream
}

/** Per-paddock fence render state driven by the disarm animation. */
private data class FenceVisual(val alpha: Float, val flash: Float)

/** Fence outline colors: armed (dark red), and the disarm flash (selection yellow). */
private val FENCE_ARMED_COLOR = Color(0xFF7A1616)
private val FENCE_DISARM_FLASH = SELECT_LOCKED

/**
 * Draws only a paddock's fence outline (and its selection tint fill). Split out
 * from the icon/label marker so every fence can be drawn in a first pass and
 * the species icons layered on top in a second pass — otherwise a neighboring
 * paddock's fence could paint over an earlier paddock's icons.
 *
 * [alpha] fades the fence out as it disarms (0 = hidden); [flash] (0..1) mixes
 * the armed dark-red toward the disarm orange for the flashing pulse.
 */
private fun DrawScope.drawPaddockFence(
    shape: PaddockShape,
    pt: (FractionalPoint) -> Offset,
    w: Float,
    selected: Boolean,
    editMode: Boolean,
    alpha: Float = 1f,
    flash: Float = 0f,
) {
    // A Building has no fence to trace — it's just its icon.
    if (shape.vertices.isEmpty() || shape.isBuilding) return
    // Fully disarmed (faded out) and not selected: nothing to draw. A selected
    // paddock still shows its highlight so it stays visible/selectable.
    if (alpha <= 0f && !selected) return
    val points = shape.toFractionalPoints()
    val lineColor = paddockLineColor(selected, editMode)
    // Fence stroke, 15% thinner than the original 0.006/0.004 of map width.
    val lineWidth = if (selected) w * 0.0051f else w * 0.0034f
    // A selected paddock keeps the selection highlight (green/yellow) so it
    // stands out; otherwise the fence is dark red, flashing toward orange while
    // disarming and fading out via [alpha].
    val fenceColor = if (selected) {
        lineColor
    } else {
        lerp(FENCE_ARMED_COLOR, FENCE_DISARM_FLASH, flash).copy(alpha = alpha)
    }

    val path = Path().apply {
        points.forEachIndexed { index, point ->
            val o = pt(point)
            if (index == 0) moveTo(o.x, o.y) else lineTo(o.x, o.y)
        }
        if (shape.closed) close()
    }
    if (selected) {
        drawPath(path, color = lineColor.copy(alpha = 0.12f))
    }
    drawPath(
        path,
        color = fenceColor,
        style = Stroke(width = lineWidth, join = StrokeJoin.Round, cap = StrokeCap.Round),
    )
}

/** Fence-status color: an alert red that stays legible on the dark backing. */
private val PADDOCK_STATUS_COLOR = Color(0xFFEB5757)

/** Red pill background for the disarm "UNARMED" label. */
private val UNARMED_LABEL_RED = Color(0xFFC0392B)

/**
 * The "UNARMED" pill (white text on a red rounded background) shown centered in
 * a paddock while its fence is disarming. [alpha] fades it out with the fence.
 */
private fun DrawScope.drawUnarmedLabel(
    centroid: Offset,
    w: Float,
    markerScale: Float,
    textMeasurer: TextMeasurer,
    alpha: Float,
) {
    val layout = textMeasurer.measure(
        text = "UNARMED",
        style = TextStyle(
            color = Color.White.copy(alpha = alpha),
            fontSize = (w * 0.0117f * markerScale).toSp(),
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center,
        ),
    )
    val padX = w * 0.009f * markerScale
    val padY = w * 0.0045f * markerScale
    val boxW = layout.size.width + 2 * padX
    val boxH = layout.size.height + 2 * padY
    drawRect(
        color = UNARMED_LABEL_RED.copy(alpha = alpha),
        topLeft = Offset(centroid.x - boxW / 2f, centroid.y - boxH / 2f),
        size = Size(boxW, boxH),
    )
    drawText(
        layout,
        topLeft = Offset(centroid.x - layout.size.width / 2f, centroid.y - layout.size.height / 2f),
    )
}

/**
 * Draws a paddock's center content — either its species icon(s) or its name +
 * fence status, per [PaddockShape.displayMode] — plus, in edit mode, its vertex
 * handles. Everything here sits ON TOP of every paddock's fence; run after
 * [drawPaddockFence] for all paddocks.
 */
private fun DrawScope.drawPaddockMarker(
    shape: PaddockShape,
    pt: (FractionalPoint) -> Offset,
    w: Float,
    textMeasurer: TextMeasurer,
    icons: List<ImageBitmap>,
    selected: Boolean,
    editMode: Boolean,
    selectedVertexIndex: Int?,
    zoom: Float = 1f,
    // Current fence visibility (0 = fully faded out). While a fence is disarming
    // (>0 and failed) an UNARMED label is shown in place of the species icons.
    fenceAlpha: Float = 1f,
) {
    if (shape.vertices.isEmpty()) return
    // Marker size dampening: the map canvas — and thus `w` — grows 1:1 with zoom,
    // so a plain `w * fraction` marker would balloon. Dividing by sqrt(zoom)
    // makes it grow sub-linearly: 2x zoom -> ~1.41x, 4x zoom -> 2x. Readable
    // zoomed out, unobtrusive zoomed in.
    val markerScale = 1f / kotlin.math.sqrt(zoom.coerceAtLeast(0.01f))
    val points = shape.toFractionalPoints()
    val lineColor = paddockLineColor(selected, editMode)
    val centroid = polygonCentroid(points.map(pt))

    // A failed (unarmed) fenced paddock hides its species icons; buildings have
    // no fence and can't fail, so they always show theirs.
    val failed = !shape.armed && !shape.isBuilding
    when (shape.displayMode) {
        PaddockDisplayMode.SpeciesIcon ->
            if (!failed) {
                drawPaddockIcons(shape, centroid, w, markerScale, lineColor, selected, icons)
            } else if (fenceAlpha > 0f) {
                // Disarming: show the UNARMED pill (fading out with the fence).
                drawUnarmedLabel(centroid, w, markerScale, textMeasurer, fenceAlpha)
            }
        PaddockDisplayMode.Name ->
            drawPaddockNameBlock(shape, centroid, w, markerScale, lineColor, selected, textMeasurer)
    }

    // Vertex handles — only shown in edit mode (they're just drag targets), and
    // never for a building: it has no fence nodes, you drag its icon instead.
    if (editMode && !shape.isBuilding) {
        val handleRadius = if (selected) w * 0.008f else w * 0.005f
        points.forEachIndexed { index, point ->
            val o = pt(point)
            val isSelectedVertex = index == selectedVertexIndex
            val r = if (isSelectedVertex) handleRadius * 1.6f else handleRadius
            drawCircle(Color.Black, radius = r * 1.3f, center = o)
            drawCircle(if (isSelectedVertex) Color(0xFFD5CD58) else lineColor, radius = r, center = o)
        }
    }
}

/** [PaddockDisplayMode.SpeciesIcon]: one species icon-disc per species, centered on [centroid]. */
private fun DrawScope.drawPaddockIcons(
    shape: PaddockShape,
    centroid: Offset,
    w: Float,
    markerScale: Float,
    lineColor: Color,
    selected: Boolean,
    icons: List<ImageBitmap>,
) {
    if (icons.isEmpty()) return
    // 15% smaller than the original 0.020 of map width.
    val iconRadius = w * 0.017f * markerScale
    val gap = w * 0.006f * markerScale
    val totalWidth = icons.size * (2 * iconRadius) + (icons.size - 1) * gap
    // A building has no outline to recolor, so the icon itself carries the
    // selection highlight — a ring around the icon cluster.
    if (selected && shape.isBuilding) {
        drawRoundRect(
            color = lineColor,
            topLeft = Offset(centroid.x - totalWidth / 2f - gap, centroid.y - iconRadius - gap),
            size = Size(totalWidth + 2 * gap, 2 * iconRadius + 2 * gap),
            cornerRadius = CornerRadius(iconRadius + gap),
            style = Stroke(width = w * 0.005f * markerScale),
        )
    }
    var discX = centroid.x - totalWidth / 2f + iconRadius
    icons.forEach { icon ->
        drawSpeciesIcon(Offset(discX, centroid.y), iconRadius, shape.carnivore, icon, w)
        discX += 2 * iconRadius + gap
    }
}

/** [PaddockDisplayMode.Name]: the paddock name, with its optional fence status beneath, centered on [centroid]. */
private fun DrawScope.drawPaddockNameBlock(
    shape: PaddockShape,
    centroid: Offset,
    w: Float,
    markerScale: Float,
    lineColor: Color,
    selected: Boolean,
    textMeasurer: TextMeasurer,
) {
    val nameLayout = textMeasurer.measure(
        text = shape.label,
        style = TextStyle(
            color = lineColor,
            fontSize = (w * 0.018f * markerScale).toSp(),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        ),
    )
    val statusLayout = shape.fenceState?.takeIf { it.isNotBlank() }?.let { state ->
        textMeasurer.measure(
            text = state.uppercase(),
            style = TextStyle(
                color = PADDOCK_STATUS_COLOR,
                fontSize = (w * 0.014f * markerScale).toSp(),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            ),
        )
    }

    // Stack name over status, vertically centered on the centroid.
    val lineGap = if (statusLayout != null) w * 0.003f * markerScale else 0f
    val blockW = maxOf(nameLayout.size.width, statusLayout?.size?.width ?: 0).toFloat()
    val blockH = nameLayout.size.height + lineGap + (statusLayout?.size?.height ?: 0)
    val blockLeft = centroid.x - blockW / 2f
    val blockTop = centroid.y - blockH / 2f
    val padX = w * 0.004f * markerScale
    val padY = w * 0.002f * markerScale

    // Dark backing so the text stays legible over the terrain.
    drawRect(
        color = Color.Black.copy(alpha = 0.45f),
        topLeft = Offset(blockLeft - padX, blockTop - padY),
        size = Size(blockW + 2 * padX, blockH + 2 * padY),
    )
    // A building has no fence outline to recolor, so when selected the label
    // block itself carries the selection highlight — a ring around it.
    if (selected && shape.isBuilding) {
        drawRoundRect(
            color = lineColor,
            topLeft = Offset(blockLeft - 2 * padX, blockTop - 2 * padY),
            size = Size(blockW + 4 * padX, blockH + 4 * padY),
            cornerRadius = CornerRadius(w * 0.006f * markerScale),
            style = Stroke(width = w * 0.005f * markerScale),
        )
    }
    drawText(nameLayout, topLeft = Offset(centroid.x - nameLayout.size.width / 2f, blockTop))
    statusLayout?.let {
        drawText(
            it,
            topLeft = Offset(centroid.x - it.size.width / 2f, blockTop + nameLayout.size.height + lineGap),
        )
    }
}

/** Area-weighted centroid of a polygon; falls back to the vertex average for degenerate shapes. */
private fun polygonCentroid(pts: List<Offset>): Offset {
    if (pts.size < 3) {
        return Offset(pts.map { it.x }.average().toFloat(), pts.map { it.y }.average().toFloat())
    }
    var area = 0f
    var cx = 0f
    var cy = 0f
    for (i in pts.indices) {
        val a = pts[i]
        val b = pts[(i + 1) % pts.size]
        val cross = a.x * b.y - b.x * a.y
        area += cross
        cx += (a.x + b.x) * cross
        cy += (a.y + b.y) * cross
    }
    if (kotlin.math.abs(area) < 1e-3f) {
        return Offset(pts.map { it.x }.average().toFloat(), pts.map { it.y }.average().toFloat())
    }
    area *= 0.5f
    return Offset(cx / (6f * area), cy / (6f * area))
}

/** Invisible hover hit-target at [position] showing [text] in a NublarOS-styled tooltip. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MarkerTooltip(
    position: FractionalPoint,
    text: String,
    widthPx: Float,
    heightPx: Float,
    hitSize: androidx.compose.ui.unit.Dp = 20.dp,
) {
    val density = LocalDensity.current
    val xDp = with(density) { (position.x * widthPx).toDp() }
    val yDp = with(density) { (position.y * heightPx).toDp() }

    TooltipArea(
        tooltip = {
            Box(
                modifier = Modifier
                    .background(Color.Black)
                    .border(1.dp, NublarColors.StatusGreen)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(text, color = Color.White, fontSize = 11.sp)
            }
        },
        tooltipPlacement = TooltipPlacement.CursorPoint(offset = DpOffset(8.dp, 8.dp)),
        delayMillis = 200,
        modifier = Modifier
            .offset(x = xDp - hitSize / 2, y = yDp - hitSize / 2)
            .size(hitSize),
    ) {
        Box(modifier = Modifier.fillMaxSize())
    }
}

private fun DrawScope.drawFacility(
    marker: FacilityMarker,
    center: Offset,
    w: Float,
    helipadIcon: ImageBitmap,
    visitorCenterIcon: ImageBitmap,
    dockIcon: ImageBitmap,
    selected: Boolean = false,
    editMode: Boolean = false,
) {
    // Blue disc + black border, matching the paddock species-icon style. Most
    // facility kinds get their own original silhouette; minor utility
    // structures (e.g. the maintenance shed) are just a smaller plain disc.
    val facilityColor = Color(0xFF397FA4)
    val icon = when (marker.kind) {
        FacilityKind.Helipad -> helipadIcon
        FacilityKind.VisitorCenter -> visitorCenterIcon
        FacilityKind.Dock -> dockIcon
        FacilityKind.MaintenanceShed -> null
    }
    val radius = if (icon == null) w * 0.011f else w * 0.020f
    // Highlight ring when selected: green = editable (edit mode on),
    // yellow = locked (selected but not editable).
    if (selected) {
        val ringColor = if (editMode) SELECT_EDITABLE else SELECT_LOCKED
        drawCircle(ringColor, radius = radius * 1.35f, center = center, style = Stroke(width = w * 0.005f))
    }
    if (icon == null) {
        drawCircle(facilityColor, radius = radius, center = center)
        drawCircle(Color.Black, radius = radius, center = center, style = Stroke(width = w * 0.003f))
    } else {
        drawIconDisc(center, radius, facilityColor, icon, w, iconFillFactor = 1.35f)
    }
}

/**
 * A colored disc + black border with a silhouette icon (tinted black) fit
 * inside, preserving aspect ratio. The building block for both paddock
 * species markers and facility markers.
 */
private fun DrawScope.drawIconDisc(
    center: Offset,
    radius: Float,
    discColor: Color,
    icon: ImageBitmap,
    w: Float,
    iconFillFactor: Float = 1.55f,
) {
    drawCircle(discColor, radius = radius, center = center)
    drawCircle(Color.Black, radius = radius, center = center, style = Stroke(width = w * 0.003f))

    val srcW = icon.width.toFloat()
    val srcH = icon.height.toFloat()
    val scale = minOf(radius * iconFillFactor / srcW, radius * iconFillFactor / srcH)
    val dstW = srcW * scale
    val dstH = srcH * scale
    drawImage(
        image = icon,
        dstOffset = androidx.compose.ui.unit.IntOffset(
            (center.x - dstW / 2f).toInt(),
            (center.y - dstH / 2f).toInt(),
        ),
        dstSize = IntSize(dstW.toInt(), dstH.toInt()),
        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.Black),
    )
}

/**
 * One silhouette icon per species in the paddock (so a paddock with several
 * species shows several icons). Unmapped species fall back to the generic
 * skull. Extend the `when` as more species icons are added.
 *
 * If a paddock has no species listed, returns a single skull so it still gets
 * a marker.
 */
private fun speciesIcons(
    shape: PaddockShape,
    skull: ImageBitmap,
    bronto: ImageBitmap,
    raptor: ImageBitmap,
    tyrannosaurus: ImageBitmap,
    triceratops: ImageBitmap,
    gallimimus: ImageBitmap,
    segisaurus: ImageBitmap,
    metriacanthosaurus: ImageBitmap,
    parasaurolophus: ImageBitmap,
    proceratosaurus: ImageBitmap,
    herrerasaurus: ImageBitmap,
    dilophosaurus: ImageBitmap,
): List<ImageBitmap> {
    if (shape.species.isEmpty()) return listOf(skull)
    return shape.species.map { name ->
        val s = name.lowercase()
        when {
            "brachiosaur" in s || "bronto" in s || "sauropod" in s -> bronto
            "raptor" in s -> raptor
            "tyrannosaur" in s || "rex" in s -> tyrannosaurus
            "triceratops" in s || "trike" in s -> triceratops
            "gallimimus" in s -> gallimimus
            "segisaurus" in s -> segisaurus
            "metriacanthosaur" in s -> metriacanthosaurus
            "parasaurolophus" in s -> parasaurolophus
            "proceratosaur" in s -> proceratosaurus
            "herrerasaur" in s -> herrerasaurus
            "dilophosaur" in s -> dilophosaurus
            else -> skull
        }
    }
}

/**
 * Species icon: red disc for carnivores, green for herbivores (film-map
 * color coding), with the original skull silhouette on top. Marks a paddock.
 */
private fun DrawScope.drawSpeciesIcon(
    center: Offset,
    radius: Float,
    carnivore: Boolean,
    skull: ImageBitmap,
    w: Float,
) {
    val discColor = if (carnivore) Color(0xFFEB0B0B) else Color(0xFF3F8F58)
    drawIconDisc(center, radius, discColor, skull, w)
}

/** Individual tracked animal — a small orange dot with a black border. */
private fun DrawScope.drawDinosaur(marker: DinosaurMarker, center: Offset, w: Float) {
    val radius = w * 0.008f
    drawCircle(Color(0xFFE8842B), radius = radius, center = center)
    drawCircle(Color.Black, radius = radius, center = center, style = Stroke(width = w * 0.002f))
}

private fun DrawScope.drawVehicle(marker: VehicleMarker, center: Offset, w: Float) {
    val size = w * 0.014f
    rotate(marker.headingDegrees, pivot = center) {
        val arrow = Path().apply {
            moveTo(center.x, center.y - size)
            lineTo(center.x - size * 0.7f, center.y + size * 0.6f)
            lineTo(center.x + size * 0.7f, center.y + size * 0.6f)
            close()
        }
        drawCircle(Color(0xFF397FA4), radius = size * 1.1f, center = center)
        drawPath(arrow, color = Color.White)
    }
}

private fun DrawScope.drawStaff(marker: StaffMarker, center: Offset, w: Float) {
    val r = w * 0.008f
    drawCircle(Color(0xFFD5CD58), radius = r, center = center)
    drawCircle(Color.Black, radius = r, center = center, style = Stroke(width = w * 0.0015f))
}
