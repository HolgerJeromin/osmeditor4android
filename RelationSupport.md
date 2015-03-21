# Introduction #

Branch 0.9 has relation support with the primary goal of keeping existing relation memberships and relations intact and consistent after elements have been edited. It currently supports explicit editing of relation tags, deletion of relations and adding turn restrictions.

An APK is available in the download section and it will be available in the google Play Store soon.


[Video showing most of the new features](http://www.youtube.com/embed/FnU-b6EZ_sE)


# Relation related New features #

  * relation membership display on tag editing form with minimal editing (deletion of elements, changing of roles, changing parent relation)
  * objects can be added to relations that exist in the downloaded data
  * spliting of ways retains relation membership in both resulting segments with the exception of restrictions where it tries to assign restriction membership to the correct part.
  * relations can be selected for tag editing and deletion by clicking on a member of the relation and selecting the relation in the popup.
  * turn restrictions can be added, in edit ("unlocked", former EasyEdit) mode select a way, then from the menu "Add restriction" then the "via" and "to" elements.
  * rendering of otherwise untagged ways that are part of a multipolygon is based on the multipolygon tags.

# OAuth #

OAuth is currently supported for the main production and development APIs, further APIs can be added on request.

New installs have OAuth enabled by default for the main OSM site, old installs need to enable OAuth by going to advanced preferences / osm api url and long pressing on the default "OpenStreetMap" entry, select "Edit" and check the OAuth checkbox.

Access token and secret can be deleted and new negation forced by de-selecting OAuth for the API in question, saving, then re-enabling.

If OAuth is enabled viewing an OSM Note will start the authorization sequence.

# Other Changes #

  * some changes to the rendering were made. landuse and tracks now have separate rendering, house color was changed. The rendering is now user selectable the old "everything black" style is available as "Default", this one as "Color". Further styles can be added, but please be aware that the current implementation is not likely to be long lived.
  * loading and saving the data state failed on 2.3 devices and emulators due to stack overflows, moving these operation to separate threads seems to have fixed it, besides being slightly hackish, the allocated stack size probably needs some tuning.
  * a long press on a preset in the tag editor will remove it from the preset list.
  * added the trivial changes for basic Samsung multi window support
  * merging of ways now tries to preserve existing way ids and uses conventional tag merging logic
  * reversing a way, either explicitly in EasyEdit mode or implicitly by merging ways with different directions, will now reverse direction dependent tags. If a oneway tag is present, the explicit reversing will not change the value of oneway, assuming that this was the point of reversing in the first place.
  * reversing a "non-reversible" way will display a warning.
  * replaced OSM bugs support with support for the new Notes API (note the XML version of the Notes API still has some issues which is the reason for the "unknown action" string in the display)
  * quick way of re-enabling GPS following: The GPS marker will turn to white (default for now) when following and back to the standard blue when following has been turned off. Clicking/touching the marker will turn following back on. Initially the following still has to be turned on via the menu.
  * "old" editing modes can be suppressed via a preference, default is to only show lock/unlocked symbol to switch from "Move" and "EasyEdit" mode
  * move/drag way
  * copy, cut and paste of nodes and ways
  * experimental geo-referenced photo layer. Vespucci will try to find photographs in DCIM, osmtracker, and Vespucci directories on internal and external storage.
