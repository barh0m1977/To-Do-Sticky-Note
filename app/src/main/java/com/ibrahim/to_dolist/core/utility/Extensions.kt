package com.ibrahim.to_dolist.core.utility

import java.time.LocalDate
import java.time.ZoneId

// casting the Long to LocalDate
fun Long.toLocalDate(): LocalDate =
    java.time.Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

