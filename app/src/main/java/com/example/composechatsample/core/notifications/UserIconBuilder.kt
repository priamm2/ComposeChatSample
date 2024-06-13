package com.example.composechatsample.core.notifications

import android.content.Context
import android.graphics.BitmapFactory
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.drawable.toBitmapOrNull
import com.example.composechatsample.core.models.User
import kotlinx.coroutines.withContext
import java.net.URL

public interface UserIconBuilder {

    public suspend fun buildIcon(user: User): IconCompat?
}

internal class DefaultUserIconBuilder(val context: Context) : UserIconBuilder {
    override suspend fun buildIcon(user: User): IconCompat? =
        user.image
            .takeUnless { it.isEmpty() }
            ?.let {
                withContext(DispatcherProvider.IO) {
                    runCatching {
                        URL(it).openStream().use {
                            RoundedBitmapDrawableFactory.create(
                                context.resources,
                                BitmapFactory.decodeStream(it),
                            )
                                .apply { isCircular = true }
                                .toBitmapOrNull()
                        }
                            ?.let(IconCompat::createWithBitmap)
                    }.getOrNull()
                }
            }
}