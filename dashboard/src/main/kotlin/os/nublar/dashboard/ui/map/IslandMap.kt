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
import androidx.compose.material.Text
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
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
    val bytes = object {}.javaClass.classLoader.getResourceAsStream(resource)!!.readBytes()
    return org.jetbrains.skia.Image.makeFromEncoded(bytes).toComposeImageBitmap()
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
    facilities: List<FacilityMarker> = SampleMapData.facilities,
    dinosaurs: List<DinosaurMarker> = SampleMapData.dinosaurs,
    vehicles: List<VehicleMarker> = SampleMapData.vehicles,
    staff: List<StaffMarker> = SampleMapData.staff,
    paddockShapes: List<PaddockShape> = emptyList(),
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
    val islandBitmap = remember { loadBitmap("island.png") }
    val skullBitmap = remember { loadBitmap("icons/dino-skull.png") }
    val brontoBitmap = remember { loadBitmap("icons/dino/brontosaurus.png") }
    val raptorBitmap = remember { loadBitmap("icons/dino/raptor.png") }
    val tyrannosaurusBitmap = remember { loadBitmap("icons/dino/tyrannosaurus.png") }
    val helipadBitmap = remember { loadBitmap("icons/facility-helipad.png") }
    val visitorCenterBitmap = remember { loadBitmap("icons/facility-visitor-center.png") }
    val dockBitmap = remember { loadBitmap("icons/facility-dock.png") }
    val textMeasurer = rememberTextMeasurer()

    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }

        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            fun pt(p: FractionalPoint) = Offset(w * p.x, h * p.y)

            drawImage(islandBitmap, dstSize = IntSize(w.toInt(), h.toInt()))

            if (MapLayer.Facilities in activeLayers) {
                facilities.forEach { marker ->
                    drawFacility(
                        marker, pt(marker.position), w, helipadBitmap, visitorCenterBitmap, dockBitmap,
                        selected = marker.id == selectedFacilityId,
                        editMode = editMode,
                    )
                }
            }
            if (MapLayer.Dinosaurs in activeLayers) {
                dinosaurs.forEach { marker -> drawDinosaur(marker, pt(marker.position), w) }
            }
            if (MapLayer.Vehicles in activeLayers) {
                vehicles.forEach { marker -> drawVehicle(marker, pt(marker.position), w) }
            }
            if (MapLayer.Staff in activeLayers) {
                staff.forEach { marker -> drawStaff(marker, pt(marker.position), w) }
            }
            // Paddocks drawn last so their outlines/handles sit on top of all
            // other markers — easier to see and select.
            if (MapLayer.Paddocks in activeLayers) {
                paddockShapes.forEach { shape ->
                    val isSel = shape.id == selectedPaddockId
                    drawPaddockShape(
                        shape, ::pt, w, textMeasurer,
                        speciesIcons(shape, skullBitmap, brontoBitmap, raptorBitmap, tyrannosaurusBitmap),
                        selected = isSel,
                        editMode = editMode,
                        selectedVertexIndex = if (isSel) selectedVertexIndex else null,
                    )
                }
            }
        }

        // Interaction overlay: paddock/facility selection (tap), vertex
        // selection (tap a dot), vertex + facility dragging, and arrow-key nudging.
        val paddocksInteractive = MapLayer.Paddocks in activeLayers && paddockShapes.isNotEmpty()
        val facilitiesInteractive = MapLayer.Facilities in activeLayers && facilities.isNotEmpty()
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
            )
        }

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
) {
    fun toFraction(offset: Offset) = FractionalPoint(offset.x / widthPx, offset.y / heightPx)

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
    // A selected vertex or facility needs focus so arrow-key events arrive.
    LaunchedEffect(selectedVertexIndex, selectedFacilityId, editMode) {
        if (editMode && (selectedVertexIndex != null || selectedFacilityId != null)) {
            focusRequester.requestFocus()
        }
    }

    // Arrow-key nudge step: 1 canvas pixel, so movement is slow/precise (key
    // auto-repeat while held moves it continuously).
    val stepX = 1f / widthPx
    val stepY = 1f / heightPx

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                if (!editModeState.value) return@onKeyEvent false   // no nudging when locked
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
                // Otherwise nudge the selected paddock vertex.
                val id = selectedState.value ?: return@onKeyEvent false
                val vi = selectedVertexState.value ?: return@onKeyEvent false
                val shape = shapesState.value.firstOrNull { it.id == id } ?: return@onKeyEvent false
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
                    val vertexHit = if (editModeState.value && selShape != null) {
                        selShape.nearestVertexWithin(startFrac, 18f / widthPx)
                    } else {
                        null
                    }

                    // In edit mode a grabbed facility or vertex can be dragged.
                    val draggable = editModeState.value && (facility != null || vertexHit != null)
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
                            }
                            fun apply(pos: Offset) {
                                val f = toFraction(pos)
                                if (facility != null) {
                                    onFacilityMovedState.value(
                                        facility.id,
                                        FractionalPoint(f.x.coerceIn(0f, 1f), f.y.coerceIn(0f, 1f)),
                                    )
                                } else if (vertexHit != null && selShape != null) {
                                    onMovedState.value(selShape.id, vertexHit, f)
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
                        // Otherwise (re)select whichever paddock contains the point.
                        else -> {
                            val hit = shapesState.value.lastOrNull {
                                pointInPolygon(startFrac, it.toFractionalPoints())
                            }
                            onSelectedState.value(hit?.id)
                            onVertexSelectedState.value(null)
                            onFacilitySelectedState.value(null)
                        }
                    }
                }
            },
    )
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

private fun DrawScope.drawPaddockShape(
    shape: PaddockShape,
    pt: (FractionalPoint) -> Offset,
    w: Float,
    textMeasurer: TextMeasurer,
    icons: List<ImageBitmap>,
    selected: Boolean,
    editMode: Boolean,
    selectedVertexIndex: Int?,
) {
    if (shape.vertices.isEmpty()) return
    val points = shape.toFractionalPoints()
    val lineColor = when {
        selected && editMode -> SELECT_EDITABLE   // editable
        selected -> SELECT_LOCKED                 // locked (selected, edit off)
        else -> Color(0xFFE2E0BF)                 // default cream
    }
    val lineWidth = if (selected) w * 0.006f else w * 0.004f

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
    drawPath(path, color = lineColor, style = Stroke(width = lineWidth, join = StrokeJoin.Round, cap = StrokeCap.Round))

    // One species icon-disc per species, laid out side-by-side centered on the
    // centroid, with the paddock name below.
    val centroid = polygonCentroid(points.map(pt))
    val iconRadius = w * 0.020f
    val gap = w * 0.006f
    if (icons.isNotEmpty()) {
        val totalWidth = icons.size * (2 * iconRadius) + (icons.size - 1) * gap
        var discX = centroid.x - totalWidth / 2f + iconRadius
        icons.forEach { icon ->
            drawSpeciesIcon(Offset(discX, centroid.y), iconRadius, shape.carnivore, icon, w)
            discX += 2 * iconRadius + gap
        }
    }

    val layout = textMeasurer.measure(
        text = shape.label,
        style = TextStyle(
            color = lineColor,
            fontSize = (w * 0.018f).toSp(),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        ),
    )
    val labelTop = centroid.y + iconRadius + w * 0.006f
    val textTopLeft = Offset(centroid.x - layout.size.width / 2f, labelTop)
    // Dark backing so the label stays legible over the terrain.
    drawRect(
        color = Color.Black.copy(alpha = 0.45f),
        topLeft = Offset(textTopLeft.x - w * 0.004f, textTopLeft.y - w * 0.002f),
        size = Size(layout.size.width + w * 0.008f, layout.size.height + w * 0.004f),
    )
    drawText(layout, topLeft = textTopLeft)

    // Vertex handles — only shown in edit mode (they're just drag targets).
    // Larger + filled when this paddock is selected; the currently-selected
    // vertex is highlighted (yellow, enlarged).
    if (editMode) {
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
    // Blue disc + black border, matching the paddock species-icon style. Each
    // facility kind gets its own original silhouette.
    val facilityColor = Color(0xFF397FA4)
    val radius = w * 0.020f
    // Highlight ring when selected: green = editable (edit mode on),
    // yellow = locked (selected but not editable).
    if (selected) {
        val ringColor = if (editMode) SELECT_EDITABLE else SELECT_LOCKED
        drawCircle(ringColor, radius = radius * 1.35f, center = center, style = Stroke(width = w * 0.005f))
    }
    val icon = when (marker.kind) {
        FacilityKind.Helipad -> helipadIcon
        FacilityKind.VisitorCenter -> visitorCenterIcon
        FacilityKind.Dock -> dockIcon
    }
    drawIconDisc(center, radius, facilityColor, icon, w, iconFillFactor = 1.35f)
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
): List<ImageBitmap> {
    if (shape.species.isEmpty()) return listOf(skull)
    return shape.species.map { name ->
        val s = name.lowercase()
        when {
            "brachiosaur" in s || "bronto" in s || "sauropod" in s -> bronto
            "raptor" in s -> raptor
            "tyrannosaur" in s || "rex" in s -> tyrannosaurus
            // "parasaurolophus" -> parasaurolophus  // TODO: add icon when the art exists
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
