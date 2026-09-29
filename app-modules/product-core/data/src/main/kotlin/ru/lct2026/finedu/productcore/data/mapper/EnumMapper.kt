package ru.lct2026.finedu.productcore.data.mapper

/** Имя enum из хранилища или JSON-контента (регистр не важен: `needs` → `NEEDS`). */
internal inline fun <reified T : Enum<T>> String.toEnum(): T = enumValueOf<T>(uppercase())
