package bumblebee.core

import bumblebee.color.Color
import bumblebee.type.*
import bumblebee.util.Converter.Companion.byteToInt
import bumblebee.util.Converter.Companion.colorToByte
import bumblebee.util.Histogram
import komat.Element
import komat.space.Cube
import kotlin.experimental.inv
import kotlin.math.floor
import kotlin.math.pow

class ImgProcessor {
    companion object {
        fun set(imgPix: ImgPix, row: Int, col: Int, color: Color): ImgPix {
            if (imgPix.colorType != color.colorType) {
                System.err.println("ERROR : ColorType does not match")
            } else {
                val byteArray: ByteArray = colorToByte(color)
                for (i: Int in 0 until imgPix.bytesPerPixel) {
                    imgPix.cube[row, col, i] = byteArray[i]
                }
            }
            return imgPix
        }

        fun crop(imgPix: ImgPix, row: Int, col: Int, width: Int, height: Int): ImgPix {

            val bytesPerPixel = imgPix.bytesPerPixel
            val cube = Cube(width, height, bytesPerPixel, Element(0.toByte()))

            for (i: Int in 0 until width) {
                for (j: Int in 0 until height) {
                    for (k: Int in 0 until bytesPerPixel) {
                        cube[i, j, k] =
                            imgPix.cube[i + row, j + col,k]
                    }
                }
            }

            imgPix.metaData.width = width
            imgPix.metaData.height = height
            imgPix.cube = cube

            return imgPix
        }

        fun resize(imgPix: ImgPix, width: Int, height: Int): ImgPix {
            val pixelCube = Cube(width, height, imgPix.bytesPerPixel, Element(0.toByte()))

            val oriW = imgPix.width
            val oriH = imgPix.height

            val widthRatioPixel = imgPix.width.toDouble() / width
            val heightRatioPixel = imgPix.height.toDouble() / height

            val processedImgPix = imgPix.pad(PadType.AVERAGE, 1).crop(1, 1, oriW + 1, oriH + 1)

            //Actual pixels that have been truncated
            val processedCube = processedImgPix.cube

            for (i in 0 until width) {
                for (j in 0 until height) {
                    for (k in 0 until imgPix.bytesPerPixel) {

                        //Ideal pixels (x, y)
                        val x = i * widthRatioPixel
                        val y = j * heightRatioPixel

                        val x0 = floor(x).toInt()
                        val x1 = x0 + 1
                        val y0 = floor(y).toInt()
                        val y1 = y0 + 1


                        val p00 = processedCube[x0, y0, k].toByte()
                        val p10 = processedCube[x1, y0, k].toByte()
                        val p01 = processedCube[x0, y1, k].toByte()
                        val p11 = processedCube[x1, y1, k].toByte()

                        //Weight
                        val fx = x - x0
                        val fy = y - y0

                        //Interpolation for x
                        val fa = p00 * (1 - fx) + p10 * fx
                        val fb = p01 * (1 - fx) + p11 * fx

                        //Interpolation for y
                        val value = (fa * (1 - fy) + fb * fy).toInt().toByte()

                        pixelCube[i, j, k] = value
                    }
                }
            }
            imgPix.metaData.width = width
            imgPix.metaData.height = height
            imgPix.cube = pixelCube

            // bilinear interpolation 완료
            return imgPix
        }

        /**
         * This function inverts the color of pixel
         * @return[ImgPix]
         * */
        fun invert(imgPix: ImgPix): ImgPix {

            val width = imgPix.width
            val height = imgPix.height
            val bytesPerPixel = imgPix.bytesPerPixel

            for (i: Int in 0 until width * height * bytesPerPixel) {
                imgPix.cube[i] = (imgPix.cube[i].toByte()).inv()
            }
            return imgPix
        }

        fun flip(imgPix: ImgPix, orientation: OrientationType): ImgPix {

            val width = imgPix.width
            val height = imgPix.height
            val bytesPerPixel = imgPix.bytesPerPixel

            val pixelCube = Cube(width, height, bytesPerPixel, Element(0.toByte()))

            when (orientation) {
                OrientationType.HORIZONTAL -> {
                    for (i: Int in 0 until height) {
                        for (j: Int in 0 until width) {
                            for (k: Int in 0 until bytesPerPixel) {
                                pixelCube[j * bytesPerPixel + k + (i * bytesPerPixel * width)] =
                                    (imgPix.cube[((i + 1) * bytesPerPixel * width - 1) - (j * bytesPerPixel + (bytesPerPixel - 1) - k)])
                            }
                        }
                    }
                }

                OrientationType.VERTICAL -> {
                    for (i: Int in 0 until height) {
                        for (j: Int in 0 until width) {
                            for (k: Int in 0 until bytesPerPixel) {
                                pixelCube[j * bytesPerPixel + k + (i * bytesPerPixel * width)] =
                                    imgPix.cube[width * (height - (i + 1)) * bytesPerPixel + j * bytesPerPixel + k]
                            }
                        }
                    }
                }
            }

            imgPix.cube = pixelCube

            return imgPix
        }

        @Deprecated("this function does nothing")
        fun rotate(imgPix: ImgPix, degree: Int): ImgPix {
            return imgPix
        }

        @Deprecated("this function does nothing")
        fun crop(imgPix: ImgPix, degree: Int): ImgPix {
            return imgPix
        }

        fun toGrayScale(imgPix: ImgPix): ImgPix {
            val oldBytesPerPixel = imgPix.bytesPerPixel

            val width = imgPix.width
            val height = imgPix.height

            imgPix.metaData.colorType = ColorType.GRAY_SCALE

            val cube = Cube(width, height, 1, Element(0.toByte()))

            for (i in 0 until width) {
                for (j in 0 until height) {
                    var integer = 0
                    for (k in 0 until oldBytesPerPixel) {
                        integer += (imgPix.cube[i, j, k].toByte()).toUByte().toInt()
                    }
                    cube[i, j, 0] = (integer / oldBytesPerPixel).toByte()
                }
            }

            imgPix.cube = cube

            return imgPix
        }

        fun threshold(imgPix: ImgPix, level: Int): ImgPix {

            if (imgPix.colorType != ColorType.GRAY_SCALE) {
                toGrayScale(imgPix)
            }

            for (i in 0 until imgPix.cube.size()) {
                val byte = if (imgPix.cube[i].toByte().byteToInt() > level) (255).toByte() else (0).toByte()
                imgPix.cube[i] = byte
            }

            return imgPix
        }

        fun threshold(imgPix: ImgPix, thresholdType: ThresholdType): ImgPix {
            if (imgPix.colorType != ColorType.GRAY_SCALE) {
                toGrayScale(imgPix)
            }

            when (thresholdType) {
                ThresholdType.OTSU -> {
                    return threshold(imgPix, Histogram(imgPix).getOtsuLevel())
                }

                else -> {}
            }

            return imgPix
        }

        fun pad(imgPix: ImgPix, padType: PadType, padSize: Int): ImgPix {

            val width = padSize + imgPix.width + padSize
            val height = padSize + imgPix.height + padSize
            val bytesPerPixel = imgPix.bytesPerPixel

            val pixelCube = Cube(width, height, bytesPerPixel, Element(0.toByte()))

            when (padType) {
                PadType.ZERO -> {
                    for (i: Int in 0 until height) {
                        for (j: Int in 0 until width) {
                            for (k: Int in 0 until bytesPerPixel) {
                                if ((i >= padSize && i < height - padSize) && (j >= padSize && j < width - padSize)) {
                                    pixelCube[j * bytesPerPixel + k + (i * bytesPerPixel * width)] =
                                        (imgPix.cube[k + (j - padSize) * bytesPerPixel + (i - padSize) * bytesPerPixel * (width - 2 * padSize)].toByte())
                                } else {
                                    pixelCube[j * bytesPerPixel + k + (i * bytesPerPixel * width)] = (0).toByte()
                                }
                            }
                        }
                    }
                }

                PadType.AVERAGE -> {
                    var averagePixel = Histogram(imgPix).getAverage(imgPix.colorType)
                    for (i: Int in 0 until height) {
                        for (j: Int in 0 until width) {
                            for (k: Int in 0 until bytesPerPixel) {
                                if ((i >= padSize && i < height - padSize) && (j >= padSize && j < width - padSize)) {
                                    pixelCube[j * bytesPerPixel + k + (i * bytesPerPixel * width)] =
                                        imgPix.cube[k + (j - padSize) * bytesPerPixel + (i - padSize) * bytesPerPixel * (width - 2 * padSize)].toByte()
                                } else {
                                    pixelCube[j * bytesPerPixel + k + (i * bytesPerPixel * width)] = averagePixel[k]
                                }
                            }
                        }
                    }
                }
            }

            imgPix.metaData.width = width
            imgPix.metaData.height = height
            imgPix.cube = pixelCube

            return imgPix
        }

        /**
         *
         * This function process image filtering : Average, Median
         *
         * @param[imgPix] The ImgPix
         * @param[filterType] The FilterType
         * @param[filterSize] The filterSize it will be odd number
         * @return[imgPix]
         */
        fun filter(imgPix: ImgPix, filterType: FilterType, filterSize: Int): ImgPix {
            return filter(imgPix, filterType, filterSize, 1.0)
        }

        fun filter(imgPix: ImgPix, filterType: FilterType, filterSize: Int, stdev: Double): ImgPix {

            val width = imgPix.width
            val height = imgPix.height
            val bytesPerPixel = imgPix.bytesPerPixel
            val pixelCube = Cube(width, height, bytesPerPixel, Element(0.toByte()))

            when (filterType) {
                FilterType.AVERAGE -> {

                    val windowSize = filterSize * filterSize
                    val halfFilterSize = filterSize / 2

                    val tempImgPix = imgPix.pad(PadType.AVERAGE, halfFilterSize)
                    val padImgPixWidth = tempImgPix.width
                    val tempPixelCube = tempImgPix.cube

                    for (i: Int in halfFilterSize until height + halfFilterSize) {
                        for (j: Int in halfFilterSize until width + halfFilterSize) {
                            for (k: Int in 0 until bytesPerPixel) {
                                var intValue = 0
                                for (l: Int in 0 until windowSize) {
                                    intValue += (tempPixelCube[((i - halfFilterSize + (l % filterSize)) * bytesPerPixel * padImgPixWidth) + ((j - halfFilterSize + (l / filterSize)) * bytesPerPixel) + k].toByte()).toUByte()
                                        .toInt()
                                }
                                pixelCube[(j - halfFilterSize) * bytesPerPixel + k + ((i - halfFilterSize) * bytesPerPixel * width)] =
                                    ((intValue / windowSize).toByte())
                            }
                        }
                    }
                }

                FilterType.MEDIAN -> {

                    val windowSize = filterSize * filterSize
                    val middleSize = windowSize / 2
                    val halfFilterSize = filterSize / 2

                    val tempImgPix = imgPix.pad(PadType.AVERAGE, halfFilterSize)
                    val padImgPixWidth = tempImgPix.width
                    val tempPixelCube = tempImgPix.cube

                    for (i: Int in halfFilterSize until height + halfFilterSize) {
                        for (j: Int in halfFilterSize until width + halfFilterSize) {
                            repeat(bytesPerPixel) { k ->
                                var uByteArray = UByteArray(windowSize)
                                repeat(windowSize) { l ->
                                    uByteArray[l] =
                                        (tempPixelCube[(((i - halfFilterSize + (l % filterSize)) * bytesPerPixel * padImgPixWidth) + ((j - halfFilterSize + (l / filterSize)) * bytesPerPixel) + k)].toByte()).toUByte()
                                }
                                uByteArray.sort()
                                pixelCube[(j - halfFilterSize) * bytesPerPixel + k + ((i - halfFilterSize) * bytesPerPixel * width)] =
                                    ((uByteArray[middleSize]).toByte())
                            }
                        }
                    }

                }

                FilterType.SHARPEN -> {
                    val padSize = 1
                    val windowSize = 9
                    val filterSize = 3
                    val strength = 9
                    val exceptStrength = -1
                    val tempImgPix = imgPix.pad(PadType.AVERAGE, padSize)
                    val padImgPixWidth = tempImgPix.width
                    val tempPixelCube = tempImgPix.cube

                    for (i: Int in padSize until height + padSize) {
                        for (j: Int in padSize until width + padSize) {
                            for (k: Int in 0 until bytesPerPixel) {
                                var intValue = 0
                                for (l: Int in 0 until windowSize) {
                                    var temp = (
                                            (tempPixelCube[(((i - padSize + (l / filterSize)) * bytesPerPixel * padImgPixWidth) + ((j - padSize + (l % filterSize)) * bytesPerPixel) + k)].toByte()).toUByte()
                                                .toInt())
                                    intValue += if (l == 4) {
                                        strength * temp
                                    } else {
                                        exceptStrength * temp
                                    }
                                }
                                pixelCube[(j - padSize) * bytesPerPixel + k + ((i - padSize) * bytesPerPixel * width)] =
                                    intValue.toByte()
                            }
                        }
                    }
                }

                FilterType.GAUSSIAN -> {
                    val padSize = ((filterSize - 1) / 2)
                    val tempImgPix = imgPix.pad(PadType.AVERAGE, padSize)
                    val padImgPixWidth = tempImgPix.width
                    val tempPixelCube = tempImgPix.cube
                    val mask = getGaussianMask(filterSize, padSize, stdev)
                    for (i: Int in padSize until height + padSize) {
                        for (j: Int in padSize until width + padSize) {
                            for (k: Int in 0 until bytesPerPixel) {
                                var sum = 0.0

                                for (l: Int in mask.indices) {
                                    for (m: Int in mask.indices) {
                                        val value =
                                            (tempPixelCube[(((i - padSize + l) * bytesPerPixel * padImgPixWidth) + ((j - padSize + m) * bytesPerPixel) + k)].toByte()).toUByte()
                                                .toInt()
                                        sum += value * mask[m][l]
                                    }
                                }
                                pixelCube[(j - padSize) * bytesPerPixel + k + ((i - padSize) * bytesPerPixel * width)] =
                                    sum.toInt().toByte()
                            }
                        }
                    }
                }

            }

            imgPix.metaData.width = width
            imgPix.metaData.height = height
            imgPix.cube = pixelCube

            return imgPix
        }

        fun getChannel(imgPix: ImgPix, chanelIndex: Int): ImgPix {
            val width = imgPix.width
            val height = imgPix.height
            val pixelCube = Cube(width, height, 1, Element(0.toByte()))
            val originPixelCube = imgPix.cube

            for (i in 0 until width) {
                for (j in 0 until height) {
                    pixelCube[i, j, 0] = originPixelCube[i, j, chanelIndex]
                }
            }

            imgPix.metaData.colorType = ColorType.GRAY_SCALE
            imgPix.cube = pixelCube

            return imgPix
        }

        private fun getGaussianMask(filterSize: Int, padSize: Int, stdev: Double): Array<Array<Double>> {
            val mask = Array(filterSize) { Array(filterSize) { 0.0 } }
            var sum = 0.0
            for (row: Int in -padSize until padSize + 1) {
                for (col: Int in -padSize until padSize + 1) {
                    val value =
                        (1.0 / (2 * Math.PI * stdev * stdev)) * Math.E.pow(-(row * row + col * col) / (2 * stdev * stdev))
                    mask[row + padSize][col + padSize] = value
                    sum += value
                }
            }

            //must do norm because pixel distance is discrete...
            for (row in 0 until filterSize) {
                for (col in 0 until filterSize) {
                    mask[row][col] /= sum
                }
            }
            return mask
        }

    }
}