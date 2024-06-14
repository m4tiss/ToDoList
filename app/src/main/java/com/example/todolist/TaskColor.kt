package com.example.todolist

enum class TaskColor(val colorName: String) {
    RED("RED"),
    BLUE("BLUE"),
    ORANGE("ORANGE");

    companion object {
        fun nextColor(current: TaskColor): TaskColor {
            return when (current) {
                RED -> BLUE
                BLUE -> ORANGE
                ORANGE -> RED
            }
        }
    }
}
