Bing Maps support first appeared in version 0.7.0.

# Introduction #

Bing Maps has several legal requirements to the use of their aerial photography.
  * Display of source attributions.
  * Display of Bing logo.
  * Link to End User Terms of Use.
  * Use a registered API key to access the data.
  * Access tile servers indirectly via metadata.

See also: [Bing Maps Imagery Editor API for aerial images in OpenStreetMap](http://www.bing.com/community/site_blogs/b/maps/archive/2010/12/01/bing-maps-aerial-imagery-in-openstreetmap.aspx)

# Details #

Everything starts with the map metadata. This is requested using the following URL:

```
http://dev.virtualearth.net/REST/v1/Imagery/Metadata/Aerial/?mapVersion=v1&key={apikey}&include=ImageryProviders&output=xml
```

Where {apikey} is replaced with the registered Bing API key.

A reduced representation of the response to that request is:

```
<?xml version="1.0" encoding="utf-8" ?> 
<Response xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns="http://schemas.microsoft.com/search/local/ws/rest/v1">
  <Copyright>Copyright © 2011 Microsoft and its suppliers. All rights reserved. This API cannot be accessed and the content and any results may not be used, reproduced or transmitted in any manner without express written permission from Microsoft Corporation.</Copyright> 
  <BrandLogoUri>http://dev.virtualearth.net/Branding/logo_powered_by.png</BrandLogoUri> 
  <StatusCode>200</StatusCode> 
  <StatusDescription>OK</StatusDescription> 
  <AuthenticationResultCode>ValidCredentials</AuthenticationResultCode> 
  <TraceId>d84ca654171b4098b9c0b821269519ec|BAYM001201|02.00.82.1400|</TraceId> 
  <ResourceSets>
    <ResourceSet>
      <EstimatedTotal>1</EstimatedTotal> 
      <Resources>
        <ImageryMetadata>
          <ImageUrl>http://ecn.{subdomain}.tiles.virtualearth.net/tiles/a{quadkey}.jpeg?g=658&mkt={culture}</ImageUrl> 
          <ImageUrlSubdomains>
            <string>t0</string> 
            <string>t1</string> 
            <string>t2</string> 
            <string>t3</string> 
          </ImageUrlSubdomains>
          <ImageWidth>256</ImageWidth> 
          <ImageHeight>256</ImageHeight> 
          <ZoomMin>1</ZoomMin> 
          <ZoomMax>19</ZoomMax> 
          <ImageryProvider>
            <Attribution>© 2010 DigitalGlobe</Attribution> 
            <CoverageArea>
              <ZoomMin>14</ZoomMin> 
              <ZoomMax>21</ZoomMax> 
              <BoundingBox>
                <SouthLatitude>-67</SouthLatitude> 
                <WestLongitude>-179.99</WestLongitude> 
                <NorthLatitude>27</NorthLatitude> 
                <EastLongitude>0</EastLongitude> 
              </BoundingBox>
            </CoverageArea>
          </ImageryProvider>
        </ImageryMetadata>
      </Resources>
    </ResourceSet>
  </ResourceSets>
</Response>
```

The actual response has multiple `ImageryProvider` elements, each of which has one or more `CoverageArea` elements.

This response provides the URL to the tile servers, list of tile server subdomains, size of the tiles, zoom level range, the list of tile sources and the areas they provide, and a suitable logo to display.

What is missing is a link to the End User Terms of Use. This is provided in a separate configuration file internal to Vespucci.