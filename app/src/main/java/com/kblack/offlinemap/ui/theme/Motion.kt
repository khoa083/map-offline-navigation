package com.kblack.offlinemap.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/**
 * Motion — the whole Material 3 spring system, the duration scale, the easing curves, and the
 * rule that picks between them (spec 1m).
 *
 * Material 3 Expressive's motion is **spring-based**, not duration-based. Compose reads it as
 * `MaterialTheme.motionScheme`; this file mirrors those exact token values so the spec maps to a
 * real API instead of to hand-tuned numbers, and so a call site that cannot take an
 * `AnimationSpec` (a MapLibre camera animation, for instance) can still read the same constants.
 *
 * ## Spatial versus effects — the distinction the whole system turns on
 *
 * - **Spatial** springs animate *position, size and shape*. They are under-damped (damping below
 *   1.0) and therefore **may overshoot**. That overshoot is the point: it is what makes a sheet
 *   feel like it has mass.
 * - **Effects** springs animate *colour, opacity and elevation*. Their damping is exactly 1.0,
 *   so they **never** overshoot. A colour that overshoots and comes back reads as a flicker or a
 *   rendering bug, never as physics — there is no real-world referent for a colour with inertia.
 *
 * Effects springs are identical in both schemes. Only the spatial springs differ.
 *
 * ## The rule for this app
 *
 * - **Static surfaces** — Home, Browse, Settings, onboarding, dialogs, sheets at rest — use the
 *   [Expressive] scheme.
 * - **Anything on screen while navigating** — turn card, ETA row, map controls, camera, off-route
 *   banner — uses the [Standard] scheme, and any property that is not position or size uses an
 *   *effects* spring so damping is 1.0 and nothing can overshoot.
 *
 * An earlier version of this file said map controls should use "no spring". The judgement was
 * right — at 60 km/h a control that squashes under the thumb reads as lag, not as delight — but
 * "no spring" is not something `motionScheme` can express. In the system's vocabulary it means
 * damping 1.0, or the standard scheme. Say it that way, or the next person implements it by
 * disabling animation entirely.
 */
object Motion {

    // ---------------------------------------------------------------------------------------
    // Expressive scheme
    // ---------------------------------------------------------------------------------------

    /**
     * Expressive scheme. The default for every surface the user reads while stopped.
     *
     * Spatial springs here are under-damped and will overshoot slightly on their way to rest.
     */
    object Expressive {
        /** Sheet enter/exit, grouped-list stagger, settings-row expansion, dialog entry. */
        const val DefaultSpatialDamping = 0.8f
        const val DefaultSpatialStiffness = 380f

        /** Switch thumb travel and its shape morph, segmented selection, nav-pill highlight. */
        const val FastSpatialDamping = 0.6f
        const val FastSpatialStiffness = 800f

        /** Empty-state and onboarding entrances — the only place a long, soft settle is welcome. */
        const val SlowSpatialDamping = 0.8f
        const val SlowSpatialStiffness = 200f

        /** State-layer fades, container-colour changes on selection, elevation changes. */
        const val DefaultEffectsDamping = 1.0f
        const val DefaultEffectsStiffness = 1600f

        /** Map-control press feedback, off-route banner colour flip, HUD value cross-fade. */
        const val FastEffectsDamping = 1.0f
        const val FastEffectsStiffness = 3800f

        /** Scrim fades behind sheets and dialogs, map-style cross-fade. */
        const val SlowEffectsDamping = 1.0f
        const val SlowEffectsStiffness = 800f
    }

    // ---------------------------------------------------------------------------------------
    // Standard scheme — everything shown while the vehicle is moving
    // ---------------------------------------------------------------------------------------

    /**
     * Standard scheme. Damping 0.9 on the spatial springs: still a spring, but it settles
     * without a visible bounce, which is what a driving surface needs.
     *
     * The effects springs are byte-identical to [Expressive]'s — 1600 / 3800 / 800 at damping
     * 1.0. They are repeated here so a call site never has to reach across schemes to find them.
     */
    object Standard {
        /** Turn-card content swap, ETA-row layout changes, camera-follow recentre. */
        const val DefaultSpatialDamping = 0.9f
        const val DefaultSpatialStiffness = 700f

        /** Zoom step, compass snap, 3D toggle — the fastest settle the system offers. */
        const val FastSpatialDamping = 0.9f
        const val FastSpatialStiffness = 1400f

        /** Unused on driving surfaces; present so the scheme is complete. */
        const val SlowSpatialDamping = 0.9f
        const val SlowSpatialStiffness = 300f

        const val DefaultEffectsDamping = 1.0f
        const val DefaultEffectsStiffness = 1600f
        const val FastEffectsDamping = 1.0f
        const val FastEffectsStiffness = 3800f
        const val SlowEffectsDamping = 1.0f
        const val SlowEffectsStiffness = 800f
    }

    // ---------------------------------------------------------------------------------------
    // Duration tokens — no value off this scale
    // ---------------------------------------------------------------------------------------

    /**
     * The Material 3 duration scale. Every duration in this app is one of these sixteen values.
     *
     * This is not pedantry. A duration typed at a call site cannot be changed system-wide later,
     * cannot be reasoned about against the rest of the system, and drifts: this file previously
     * carried `MapControlDurationMs = 140`, described as "the midpoint of the spec's 120-160ms
     * range" — a number that exists nowhere in Material 3 and could not be traced back to any
     * decision.
     */
    object Duration {
        const val Short1 = 50
        const val Short2 = 100

        /** Map-control press-to-state, zoom step. Replaces the old off-scale 140ms. */
        const val Short3 = 150

        /** Compass snap, 3D toggle, state-layer fade in and out. */
        const val Short4 = 200

        /** Camera recentre. */
        const val Medium1 = 250

        /** Sheet expand and collapse, nav-pill highlight slide. */
        const val Medium2 = 300
        const val Medium3 = 350

        /** Camera tilt into 3D navigation — the slowest camera move in the app. */
        const val Medium4 = 400

        const val Long1 = 450

        /** Dialog entry, download-complete celebration. */
        const val Long2 = 500
        const val Long3 = 550
        const val Long4 = 600

        const val ExtraLong1 = 700
        const val ExtraLong2 = 800
        const val ExtraLong3 = 900
        const val ExtraLong4 = 1000
    }

    // ---------------------------------------------------------------------------------------
    // Easing — exact curves
    // ---------------------------------------------------------------------------------------

    /**
     * The Material 3 easing curves, with their exact control points.
     *
     * Named curves only. An earlier revision of the spec asked for "linear-ish" camera motion,
     * which is not something anyone can build — if a curve is worth specifying it is worth
     * specifying exactly.
     */
    object Easings {
        /** Sheet and dialog transitions on static surfaces. */
        val Emphasized: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

        /** Elements entering the screen: sheet up, banner down. */
        val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)

        /** Elements leaving: sheet dismissal, banner exit. */
        val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

        /** Default for driving surfaces. */
        val Standard: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

        /** Map-control press and zoom step. */
        val StandardDecelerate: Easing = CubicBezierEasing(0.0f, 0.0f, 0.0f, 1.0f)

        /** Controls fading out as the HUD takes over. */
        val StandardAccelerate: Easing = CubicBezierEasing(0.3f, 0.0f, 1.0f, 1.0f)

        /**
         * Camera pan and tilt. Constant speed on purpose: the map moving under the driver must
         * track their real motion, and any acceleration curve makes the map appear to speed up
         * and slow down independently of the vehicle.
         *
         * This is the same reasoning that drives [rememberAnimatedPuckLocation]
         * (`ui/screen/overview/component/AnimatedLocationPuck.kt`) and the route-line reveal:
         * anything whose motion the user can compare against the real world is linear.
         */
        val Linear: Easing = CubicBezierEasing(0.0f, 0.0f, 1.0f, 1.0f)
    }

    // ---------------------------------------------------------------------------------------
    // Ready-made specs
    // ---------------------------------------------------------------------------------------

    /** Expressive spatial spring — sheets, dialogs, list stagger, settings rows. */
    fun <T> expressiveSpatial(): FiniteAnimationSpec<T> =
        spring(Expressive.DefaultSpatialDamping, Expressive.DefaultSpatialStiffness)

    /** Expressive fast spatial — switch thumb travel and its shape morph, segmented selection. */
    fun <T> expressiveFastSpatial(): FiniteAnimationSpec<T> =
        spring(Expressive.FastSpatialDamping, Expressive.FastSpatialStiffness)

    /** Expressive slow spatial — empty-state and onboarding entrances only. */
    fun <T> expressiveSlowSpatial(): FiniteAnimationSpec<T> =
        spring(Expressive.SlowSpatialDamping, Expressive.SlowSpatialStiffness)

    /** Effects spring — colour, opacity, elevation. Identical in both schemes. Never overshoots. */
    fun <T> effects(): FiniteAnimationSpec<T> =
        spring(Expressive.DefaultEffectsDamping, Expressive.DefaultEffectsStiffness)

    /** Fast effects — map-control press feedback, off-route colour flip, HUD cross-fade. */
    fun <T> fastEffects(): FiniteAnimationSpec<T> =
        spring(Expressive.FastEffectsDamping, Expressive.FastEffectsStiffness)

    /** Slow effects — scrim fades behind sheets and dialogs, map-style cross-fade. */
    fun <T> slowEffects(): FiniteAnimationSpec<T> =
        spring(Expressive.SlowEffectsDamping, Expressive.SlowEffectsStiffness)

    /** Standard spatial — turn-card content swap, ETA-row layout, camera-follow recentre. */
    fun <T> drivingSpatial(): FiniteAnimationSpec<T> =
        spring(Standard.DefaultSpatialDamping, Standard.DefaultSpatialStiffness)

    /** Standard fast spatial — zoom step, compass snap, 3D toggle. */
    fun <T> drivingFastSpatial(): FiniteAnimationSpec<T> =
        spring(Standard.FastSpatialDamping, Standard.FastSpatialStiffness)

    /** Map-control press: [Duration.Short3] on [Easings.StandardDecelerate]. */
    fun <T> mapControlTween(): FiniteAnimationSpec<T> =
        tween(Duration.Short3, easing = Easings.StandardDecelerate)

    /** Camera recentre: [Duration.Medium1], linear, so map motion tracks real motion. */
    fun <T> cameraRecentreTween(): FiniteAnimationSpec<T> =
        tween(Duration.Medium1, easing = Easings.Linear)

    /** Camera tilt into 3D navigation: [Duration.Medium4], linear, same reason. */
    fun <T> cameraTiltTween(): FiniteAnimationSpec<T> =
        tween(Duration.Medium4, easing = Easings.Linear)

    /** State-layer fade in and out: [Duration.Short4] on an effects curve. */
    fun <T> stateLayerTween(): FiniteAnimationSpec<T> =
        tween(Duration.Short4, easing = Easings.Standard)
}

/*
 * Progress indicator rule (spec 1m).
 *
 * The Material 3 Expressive wavy indicator (`LinearWavyProgressIndicator` /
 * `CircularWavyProgressIndicator`) is reserved for STATIC WAITS: offline-map download, the unzip
 * step, routing-engine load, and route calculation. Everything shown while navigating - route
 * progress, ETA - uses a straight track.
 *
 * The reason is measurable rather than aesthetic: a moving wave underneath a number the driver
 * is reading at a glance costs an extra fixation, and fixations while driving are the budget the
 * whole HUD is designed around.
 *
 * The track height for the straight variant lives in Spacing.progressTrackHeight, not here. It
 * was briefly declared in both files during this pass, which is the exact failure a token system
 * exists to prevent: two sources for one value drift the moment either is edited. Sizes belong
 * to Spacing; this file owns timing only.
 */
