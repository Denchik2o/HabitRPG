package com.example.habitrpg.utils

import com.example.habitrpg.R

object SpriteUtils {

    fun getSpriteResId(spriteName: String): Int {
        return when (spriteName) {
            "sprite_warrior_base" -> R.drawable.sprite_warrior_base
            "sprite_archer_base" -> R.drawable.sprite_archer_base
            "sprite_mage_base" -> R.drawable.sprite_mage_base

            "item_helmet_leather" -> R.drawable.item_helmet_leather
            "item_breastplate_leather" -> R.drawable.item_breastplate_leather
            "item_greaves_leather" -> R.drawable.item_greaves_leather

            "item_sword_wooden" -> R.drawable.item_sword_wooden
            "item_bow_oak" -> R.drawable.item_bow_oak
            "item_staff_apprentice" -> R.drawable.item_staff_apprentice
            "item_ring_copper" -> R.drawable.item_ring_copper

            else -> R.drawable.null_sprite
        }
    }

    // Получение базового спрайта для класса
    fun getBaseSpriteForClass(characterClass: String): Int {
        return when (characterClass) {
            "WARRIOR" -> R.drawable.sprite_warrior_base
            "ARCHER" -> R.drawable.sprite_archer_base
            "MAGE" -> R.drawable.sprite_mage_base
            else -> R.drawable.null_sprite
        }
    }

    // Определяем слой для предмета
    fun getItemLayer(itemType: com.example.habitrpg.data.model.ItemType): Int {
        return when (itemType) {
            com.example.habitrpg.data.model.ItemType.HELMET,
            com.example.habitrpg.data.model.ItemType.BREASTPLATE,
            com.example.habitrpg.data.model.ItemType.GREAVES -> 1 // Средний слой (броня)

            com.example.habitrpg.data.model.ItemType.WEAPON,
            com.example.habitrpg.data.model.ItemType.ACCESSORY -> 2 // Верхний слой (оружие и аксессуары)

            else -> 0 // Расходники и прочее не отображаются на персонаже
        }
    }
}