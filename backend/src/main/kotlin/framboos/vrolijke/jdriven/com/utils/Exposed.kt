package framboos.vrolijke.jdriven.com.utils

import org.jetbrains.exposed.sql.FieldSet
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.selectAll

fun FieldSet.exists(where: SqlExpressionBuilder.() -> Op<Boolean>) =
    !this.selectAll().where(where).empty()
