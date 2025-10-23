package com.example.habitrpg.data.model

import org.junit.Assert.*
import org.junit.Test

class CharacterTest {

    @Test
    fun `character should calculate correct exp for next level`() {
        val character = Character(level = 1, exp = 0)
        assertEquals(125, character.getExpForNextLevel())

        val characterLevel5 = Character(level = 5, exp = 0)
        assertEquals(225, characterLevel5.getExpForNextLevel())
    }

    @Test
    fun `character should level up when exp reaches threshold`() {
        val character = Character(level = 1, exp = 125)
        val leveledUp = character.checkLevelUp()

        assertEquals(2, leveledUp.level)
        assertEquals(0, leveledUp.exp)
        assertEquals(110, leveledUp.maxHp)
        assertEquals(55, leveledUp.maxMp)
    }

    @Test
    fun `character should not level up when exp is insufficient`() {
        val character = Character(level = 1, exp = 100)
        val result = character.checkLevelUp()

        assertEquals(1, result.level)
        assertEquals(100, result.exp)
    }

    @Test
    fun `character should handle multiple level ups`() {
        val character = Character(level = 1, exp = 300)
        val result = character.checkLevelUp()

        assertTrue(result.level > 1)
        assertTrue(result.exp < 300)
    }
}