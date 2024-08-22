package com.asif.kmmauthorizedimageprofiles

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform