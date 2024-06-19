package com.example.composechatsample.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.example.composechatsample.R

public interface ReactionIconFactory {

    public fun isReactionSupported(type: String): Boolean

    @Composable
    public fun createReactionIcon(type: String): ReactionIcon

    @Composable
    public fun createReactionIcons(): Map<String, ReactionIcon>

    public companion object {
        public fun defaultFactory(): ReactionIconFactory = DefaultReactionIconFactory()
    }
}

private class DefaultReactionIconFactory(
    private val supportedReactions: Map<String, ReactionDrawable> = mapOf(
        THUMBS_UP to ReactionDrawable(
            iconResId = R.drawable.stream_compose_ic_reaction_thumbs_up,
            selectedIconResId = R.drawable.stream_compose_ic_reaction_thumbs_up_selected,
        ),
        LOVE to ReactionDrawable(
            iconResId = R.drawable.stream_compose_ic_reaction_love,
            selectedIconResId = R.drawable.stream_compose_ic_reaction_love_selected,
        ),
        LOL to ReactionDrawable(
            iconResId = R.drawable.stream_compose_ic_reaction_lol,
            selectedIconResId = R.drawable.stream_compose_ic_reaction_lol_selected,
        ),
        WUT to ReactionDrawable(
            iconResId = R.drawable.stream_compose_ic_reaction_wut,
            selectedIconResId = R.drawable.stream_compose_ic_reaction_wut_selected,
        ),
        THUMBS_DOWN to ReactionDrawable(
            iconResId = R.drawable.stream_compose_ic_reaction_thumbs_down,
            selectedIconResId = R.drawable.stream_compose_ic_reaction_thumbs_down_selected,
        ),
    ),
) : ReactionIconFactory {

    override fun isReactionSupported(type: String): Boolean {
        return supportedReactions.containsKey(type)
    }

    @Composable
    override fun createReactionIcon(type: String): ReactionIcon {
        val reactionDrawable = requireNotNull(supportedReactions[type])
        return ReactionIcon(
            painter = painterResource(reactionDrawable.iconResId),
            selectedPainter = painterResource(reactionDrawable.selectedIconResId),
        )
    }

    @Composable
    override fun createReactionIcons(): Map<String, ReactionIcon> {
        return supportedReactions.mapValues {
            createReactionIcon(it.key)
        }
    }

    companion object {
        private const val LOVE: String = "love"
        private const val THUMBS_UP: String = "like"
        private const val THUMBS_DOWN: String = "sad"
        private const val LOL: String = "haha"
        private const val WUT: String = "wow"
    }
}

public data class ReactionDrawable(
    @DrawableRes public val iconResId: Int,
    @DrawableRes public val selectedIconResId: Int,
)

public data class ReactionIcon(
    val painter: Painter,
    val selectedPainter: Painter,
) {

    public fun getPainter(isSelected: Boolean): Painter {
        return if (isSelected) selectedPainter else painter
    }
}