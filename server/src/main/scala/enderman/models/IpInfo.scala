package enderman.models

case class IpInfo(
                 status: String,
                 country: String,
                 countryCode: String,
                 region: String,
                 regionName: String,
                 city: String,
                 zip: String,
                 lat: Int,
                 lon: Int,
                 timezone: String,
                 isp: String,
                 org: String,
                 as: String,
                 query: String,
                 )
