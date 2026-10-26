/*
 * Room database setup for BudgetBoss.
 * It registers the app entities and creates a single shared database instance for local data storage.
 */
package com.example.budgetboss.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        ExpenseEntity::class,
        BudgetGoalEntity::class,
        AlertSettingsEntity::class,
        RecurringTemplateEntity::class,
        AchievementEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class BudgetBossDatabase : RoomDatabase() {
    abstract fun budgetDao(): BudgetBossDao

    companion object {
        @Volatile
        private var instance: BudgetBossDatabase? = null

        fun getInstance(context: Context): BudgetBossDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    BudgetBossDatabase::class.java,
                    "budget_boss.db",
                ).fallbackToDestructiveMigration(dropAllTables = true).build().also {
                    instance = it
                }
            }
        }
    }
}

