package com.pethealth.passport.di

import android.content.Context
import com.pethealth.passport.data.local.AppDatabase
import com.pethealth.passport.data.repository.CommunityRepository
import com.pethealth.passport.data.repository.ContentRepository
import com.pethealth.passport.data.repository.HealthRepository
import com.pethealth.passport.data.repository.PetRepository
import com.pethealth.passport.data.repository.SettingsRepository
import com.pethealth.passport.data.seed.DatabaseSeeder
import com.pethealth.passport.util.ImageStore

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    val database: AppDatabase = AppDatabase.get(appContext)
    val settings: SettingsRepository = SettingsRepository(appContext)
    val imageStore: ImageStore = ImageStore(appContext)
    val petRepository: PetRepository = PetRepository(database.petDao())
    val healthRepository: HealthRepository = HealthRepository(database.healthDao(), database.petDao())
    val communityRepository: CommunityRepository = CommunityRepository(
        database.communityDao(),
        appContext,
        settings,
        imageStore
    )
    val contentRepository: ContentRepository = ContentRepository(settings)
    val seeder: DatabaseSeeder = DatabaseSeeder(database, settings)
}
