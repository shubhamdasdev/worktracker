package com.example.worktracker.ui.theme

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.IntOffset

/**
 * Standardized animations for WorkTracker app to ensure consistent motion design
 */
object WorkTrackerAnimations {
    // Duration constants
    private const val SHORT_DURATION = 150
    private const val MEDIUM_DURATION = 300
    private const val LONG_DURATION = 500

    // Spring constants
    private val springSpec = spring<IntOffset>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow,
        visibilityThreshold = IntOffset.VisibilityThreshold
    )

    // Fade animations
    val fadeIn = fadeIn(
        animationSpec = tween(
            durationMillis = MEDIUM_DURATION,
            easing = FastOutSlowInEasing
        )
    )

    val fadeOut = fadeOut(
        animationSpec = tween(
            durationMillis = MEDIUM_DURATION,
            easing = FastOutSlowInEasing
        )
    )

    // Scale animations
    val scaleIn = scaleIn(
        initialScale = 0.9f,
        animationSpec = tween(
            durationMillis = MEDIUM_DURATION,
            easing = FastOutSlowInEasing
        )
    )

    val scaleOut = scaleOut(
        targetScale = 0.9f,
        animationSpec = tween(
            durationMillis = MEDIUM_DURATION,
            easing = FastOutSlowInEasing
        )
    )

    // Combined animations for UI elements
    val fadeInWithScale: EnterTransition = fadeIn + scaleIn
    val fadeOutWithScale: ExitTransition = fadeOut + scaleOut

    // Slide animations for navigation
    val slideInFromRight = slideInHorizontally(
        animationSpec = tween(MEDIUM_DURATION),
        initialOffsetX = { fullWidth -> fullWidth }
    ) + fadeIn(tween(MEDIUM_DURATION))

    val slideOutToLeft = slideOutHorizontally(
        animationSpec = tween(MEDIUM_DURATION),
        targetOffsetX = { fullWidth -> -fullWidth }
    ) + fadeOut(tween(MEDIUM_DURATION))

    val slideInFromLeft = slideInHorizontally(
        animationSpec = tween(MEDIUM_DURATION),
        initialOffsetX = { fullWidth -> -fullWidth }
    ) + fadeIn(tween(MEDIUM_DURATION))

    val slideOutToRight = slideOutHorizontally(
        animationSpec = tween(MEDIUM_DURATION),
        targetOffsetX = { fullWidth -> fullWidth }
    ) + fadeOut(tween(MEDIUM_DURATION))

    // Vertical animations for expanding/collapsing content
    val expandVertically = expandVertically(
        animationSpec = tween(MEDIUM_DURATION),
        expandFrom = Alignment.Top
    ) + fadeIn(tween(MEDIUM_DURATION))

    val collapseVertically = slideOutVertically(
        animationSpec = tween(MEDIUM_DURATION),
        targetOffsetY = { fullHeight -> -fullHeight }
    ) + fadeOut(tween(MEDIUM_DURATION))

    // Spring-based animations for more natural motion
    val springInFromBottom = slideInVertically(
        animationSpec = springSpec,
        initialOffsetY = { fullHeight -> fullHeight }
    ) + fadeIn()

    val springOutToBottom = slideOutVertically(
        animationSpec = springSpec,
        targetOffsetY = { fullHeight -> fullHeight }
    ) + fadeOut()

    // Navigation transitions
    val enterTransition: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        slideInFromRight
    }

    val exitTransition: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        slideOutToLeft
    }

    val popEnterTransition: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        slideInFromLeft
    }

    val popExitTransition: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        slideOutToRight
    }

    // Content transform for replacing content with animation
    fun contentTransform(): ContentTransform {
        return fadeIn + scaleIn togetherWith fadeOut + scaleOut
    }
}
