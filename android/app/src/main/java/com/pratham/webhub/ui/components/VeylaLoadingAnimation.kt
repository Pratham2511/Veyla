package com.pratham.webhub.ui.components

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ── Brand Colors (from approved master reference) ─────────────────────────────

/** Warm ivory/cream — the front/left folded plane. */
private val VeylaIvory = Color(0xFFF0E8DE)

/** Muted green/slate — the back/right folded plane. */
private val VeylaSlate = Color(0xFF475E58)

// ── Animation Timing ──────────────────────────────────────────────────────────

/** Full cycle: fill → hold implied by reverse easing → reverse. */
private const val CYCLE_MS = 2400

// ── Geometry ──────────────────────────────────────────────────────────────────

/*
 * The approved Veyla mark is a folded-V oriented with the apex at the
 * upper-left and the two arms opening toward the bottom-right.
 *
 * Normalized key vertices (0–1 within the content bounding box):
 *
 *   Apex (top-left corner):      (0.00, 0.00)
 *   Right arm top edge end:     (0.25, 0.00)
 *   Right arm far end:          (1.00, 0.15)
 *   Right arm bottom edge:      (0.73, 0.77)
 *   Bottom-right V tip:         (0.48, 0.99)
 *   Bottom-left V tip:          (0.45, 0.99)
 *   Left arm inner edge:        (0.00, 0.00) back to apex
 *
 * The fold divides the shape into two overlapping planes:
 *   - Slate (back/right): wider, extends to x=1.0
 *   - Ivory (front/left): narrower, roughly x=0.0–0.75
 *   - Overlap zone: roughly x=0.37–0.75, y=0.13–0.99
 */

/**
 * Veyla-branded loading animation.
 *
 * Draws the approved folded-V mark using Compose [Path] geometry.
 * The animation progressively reveals the mark from top to bottom
 * (following the V's opening direction), then reverses.
 *
 * **No PNG frames, GIFs, or image sequences are used.**
 *
 * The animation respects system animation scale settings through
 * the Compose animation system.
 *
 * @param modifier Optional modifier for sizing / positioning.
 * @param size Display size of the loading mark.
 */
@Composable
fun VeylaLoadingAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "veyla_loading")

    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = CYCLE_MS,
                easing = EaseInOut
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "veyla_fill"
    )

    Canvas(modifier = modifier.size(size)) {
        drawVeylaMark(progress)
    }
}

// ── Internal Drawing ──────────────────────────────────────────────────────────

/**
 * Draws the folded-V at the given [progress] (0 = invisible, 1 = fully visible).
 *
 * The reveal sweeps from top to bottom, which naturally follows the V's
 * geometry since the mark opens downward from the apex.
 */
private fun DrawScope.drawVeylaMark(progress: Float) {
    if (progress <= 0f) return

    val w = size.width
    val h = size.height

    // ── Slate (back/right) plane path ────────────────────────────────────
    // Outer edge: apex → top-right → far-right → bottom-right tip
    // Inner edge: bottom-right tip → back to apex
    val slatePath = Path().apply {
        moveTo(w * 0.00f, h * 0.00f)   // apex
        lineTo(w * 0.25f, h * 0.00f)   // top edge of right arm
        lineTo(w * 1.00f, h * 0.15f)   // far right end of right arm
        lineTo(w * 0.73f, h * 0.77f)   // right arm curves down
        lineTo(w * 0.48f, h * 0.99f)   // bottom-right tip of V
        lineTo(w * 0.45f, h * 0.99f)   // bottom-left tip of V
        // Inner edge of slate (behind ivory)
        lineTo(w * 0.42f, h * 0.90f)
        lineTo(w * 0.37f, h * 0.75f)
        lineTo(w * 0.30f, h * 0.53f)
        lineTo(w * 0.24f, h * 0.35f)
        lineTo(w * 0.13f, h * 0.13f)
        lineTo(w * 0.00f, h * 0.00f)   // back to apex
        close()
    }

    // ── Ivory (front/left) plane path ────────────────────────────────────
    // This plane overlaps the slate, creating the fold effect
    val ivoryPath = Path().apply {
        moveTo(w * 0.00f, h * 0.00f)   // apex
        // Left edge of the mark (goes straight down)
        lineTo(w * 0.00f, h * 0.01f)
        lineTo(w * 0.00f, h * 0.05f)
        lineTo(w * 0.05f, h * 0.18f)
        lineTo(w * 0.16f, h * 0.38f)
        lineTo(w * 0.28f, h * 0.58f)
        lineTo(w * 0.40f, h * 0.78f)
        lineTo(w * 0.45f, h * 0.99f)   // bottom-left tip of V
        lineTo(w * 0.48f, h * 0.99f)   // bottom-right tip (shared)
        // Inner edge going back up (the fold line)
        lineTo(w * 0.55f, h * 0.85f)
        lineTo(w * 0.60f, h * 0.72f)
        lineTo(w * 0.63f, h * 0.58f)
        lineTo(w * 0.58f, h * 0.42f)
        lineTo(w * 0.50f, h * 0.28f)
        lineTo(w * 0.37f, h * 0.13f)
        lineTo(w * 0.25f, h * 0.02f)
        lineTo(w * 0.00f, h * 0.00f)   // back to apex
        close()
    }

    // ── Progressive reveal (top-to-bottom clip) ─────────────────────────
    val revealBottom = h * progress.coerceAtMost(1f)

    clipPath(Path().apply {
        addRect(Rect(0f, 0f, w, revealBottom))
    }) {
        // Draw slate first (it's behind), then ivory on top
        drawPath(slatePath, VeylaSlate)
        drawPath(ivoryPath, VeylaIvory)
    }
}
