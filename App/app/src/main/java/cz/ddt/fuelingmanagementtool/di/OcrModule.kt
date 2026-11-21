package cz.ddt.fuelingmanagementtool.di

import cz.ddt.fuelingmanagementtool.feature.camera.FuelStandImageProcessor
import cz.ddt.fuelingmanagementtool.feature.camera.ImageCropper
import cz.ddt.fuelingmanagementtool.feature.camera.OcrTextRecognizer
import cz.ddt.fuelingmanagementtool.feature.camera.PumpOcrParser
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object OcrModule {

    @Provides
    @Singleton
    fun provideImageCropper(): ImageCropper = ImageCropper()

    @Provides
    @Singleton
    fun providePumpOcrParser(): PumpOcrParser = PumpOcrParser()

    @Provides
    fun provideOcrTextRecognizer(): OcrTextRecognizer = OcrTextRecognizer()

    @Provides
    fun provideFuelStandImageProcessor(
        imageCropper: ImageCropper,
        textRecognizer: OcrTextRecognizer,
        pumpOcrParser: PumpOcrParser
    ): FuelStandImageProcessor = FuelStandImageProcessor(
        imageCropper = imageCropper,
        textRecognizer = textRecognizer,
        pumpOcrParser = pumpOcrParser
    )
}
