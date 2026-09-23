package com.quenazapps.bibleriddles.ui

import android.view.View
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.quenazapps.bibleriddles.R
import pl.droidsonroids.gif.GifDrawable

@Composable
internal fun MenuGifBackground(modifier: Modifier = Modifier) {
    if (LocalInspectionMode.current) {
        Image(
            painter = painterResource(R.mipmap.main_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
        return
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    AndroidView(
        factory = { context ->
            val gif = GifDrawable(context.resources, R.raw.main_menu_background).apply {
                // Zero means infinite repetition, overriding the GIF's embedded loop count.
                loopCount = 0
                stop()
            }
            val observer = object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) = gif.start()
                override fun onStop(owner: LifecycleOwner) = gif.stop()
            }
            ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
                setImageDrawable(gif)
                tag = observer
                lifecycle.addObserver(observer)
            }
        },
        onRelease = { view ->
            lifecycle.removeObserver(view.tag as DefaultLifecycleObserver)
            val gif = view.drawable as GifDrawable
            view.setImageDrawable(null)
            gif.recycle()
        },
        modifier = modifier,
    )
}
