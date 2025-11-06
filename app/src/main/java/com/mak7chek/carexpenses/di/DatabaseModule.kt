package com.mak7chek.carexpenses.di

import android.content.Context
import androidx.room.Room
import com.mak7chek.carexpenses.data.local.AppDatabase
import com.mak7chek.carexpenses.data.local.dao.TripDao
import com.mak7chek.carexpenses.data.local.dao.VehicleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "car_expenses.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }



    @Provides
    @Singleton
    fun provideVehicleDao(appDatabase: AppDatabase): VehicleDao {
        return appDatabase.vehicleDao()
    }

    @Provides
    @Singleton
    fun provideTripDao(appDatabase: AppDatabase): TripDao {
        return appDatabase.tripDao()
    }
}