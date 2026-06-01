package com.humotron.app.util

import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import java.io.IOException
import java.io.InputStream

object SvgRegistry {
    private var isRegistered = false

    @Synchronized
    fun register(context: Context) {
        if (isRegistered) return
        try {
            Glide.get(context).registry.append(
                InputStream::class.java,
                PictureDrawable::class.java,
                SvgDecoder()
            )
            isRegistered = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class SvgDecoder : ResourceDecoder<InputStream, PictureDrawable> {
    override fun handles(source: InputStream, options: Options): Boolean = true

    override fun decode(
        source: InputStream,
        width: Int,
        height: Int,
        options: Options
    ): Resource<PictureDrawable>? {
        try {
            val svg = SVG.getFromInputStream(source)
            if (width > 0 && height > 0) {
                svg.documentWidth = width.toFloat()
                svg.documentHeight = height.toFloat()
            }
            val picture = svg.renderToPicture()
            val drawable = PictureDrawable(picture)
            return SimpleResource(drawable)
        } catch (ex: SVGParseException) {
            throw IOException("Cannot load SVG from stream", ex)
        }
    }
}

fun ImageView.loadImage(url: String?, @DrawableRes placeholder: Int? = null) {
    if (url.isNullOrEmpty()) {
        if (placeholder != null) {
            setImageResource(placeholder)
        } else {
            setImageDrawable(null)
        }
        return
    }

    if (url.contains(".svg", ignoreCase = true)) {
        // Dynamically register SVG decoder with Glide if loading an SVG
        SvgRegistry.register(context.applicationContext)
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    } else {
        setLayerType(View.LAYER_TYPE_NONE, null)
    }

    var builder = Glide.with(context).asDrawable().load(url)
    if (placeholder != null) {
        builder = builder.placeholder(placeholder).error(placeholder)
    }
    builder.into(this)
}
