package uz.napa.foxmedia.util

import uz.napa.foxmedia.response.regions.Province
import uz.napa.foxmedia.response.regions.Region

data class ProvinceData(
    val province: ArrayList<Province>,
    val provinceName: Array<String?>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProvinceData

        if (province != other.province) return false
        if (!provinceName.contentEquals(other.provinceName)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = province.hashCode()
        result = 31 * result + provinceName.contentHashCode()
        return result
    }
}

data class RegionData(
    val region: ArrayList<Region>,
    val regionName: Array<String?>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RegionData

        if (region != other.region) return false
        if (!regionName.contentEquals(other.regionName)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = region.hashCode()
        result = 31 * result + regionName.contentHashCode()
        return result
    }
}