<a href="http://insideout.io"><img src="https://insideout10.github.io/silk-tripadvisor-plugin/images/insideout10-opensource.png" /></a>

TripAdvisor Silk Plugin
=======================

The TripAdvisor Silk Plugin extends Silk to reconcile with TripAdvisor locations.

Data publishers that need to link their data to 3rd party platforms such as TripAdvisor can use this plugin in their
existing or new Silk workflow to match Point of Interests (POIs) with TripAdvisor locations, adding the TripAdvisor
location id or URL to their data.

*Table of Contents*

 * [Install](#install)
 * [Configure](#configure)
  * [Using the Workbench](#using-the-workbench)
  * [Using the project configuration](#using-the-project-configuration)
  * [Using Single Machine](#using-single-machine)

## Install

Compile the library to a JAR file and copy it to Silk plugins folder (`~/.silk/plugins`).


## Configure

The TripAdvisor plugin supports the following parameters:

 * *appKey*: the TripAdvisor app key,
 * *prefix*: the prefix to prepend to the TripAdvisor location id (if you want to link to TripAdvisor web site, use http://tripadvisor.com/),
 * *limit*: the maximum number of results (use 1 to get the best match).


If you don't have a TripAdvisor app key, you can request one here: [TripAdvisor web site](https://developer-tripadvisor.com/content-api/request-api-access/)


### Using the Workbench

The TripAdvisor plugin can be used both in *transforms* and in *linking*. The following example covers the *transform*
configuration. For this example to work the [silk-geocoding-plugin](http://github.com/insideout10/silk-geocoding-plugin)
is also needed.

Create a new transform by clicking the *transform* button:

<img src="https://insideout10.github.io/silk-tripadvisor-plugin/images/workbench_step_1.png" />

Name the transform, choose a source dataset and set a restriction:

<img src="https://insideout10.github.io/silk-tripadvisor-plugin/images/workbench_step_2.png" />

Once created, open the transform by clicking on the *open* button:

<img src="https://insideout10.github.io/silk-tripadvisor-plugin/images/workbench_step_3.png" />

The editor will open, click on the *Add Rule* button:

<img src="https://insideout10.github.io/silk-tripadvisor-plugin/images/workbench_step_4.png" />

A new rule is created. Name it and set the *Target Property*. Then click on the wrench icon on the top right corner to
open the editor:

<img src="https://insideout10.github.io/silk-tripadvisor-plugin/images/workbench_step_5.png" />

The toolbox contains the paths and the required transformations in the API category. The geocoding transform is required
for this example, it can be downloaded from the [silk-geocoding-plugin](http://github.com/insideout10/silk-geocoding-plugin)
project.

<img src="https://insideout10.github.io/silk-tripadvisor-plugin/images/workbench_toolbox.png" />

From the editor, add the following source paths. In our example we're using the *schema.org* in the source dataset:

 1. street address, `?a/schema:address/schema:streetAddress`,
 2. city, `?a/schema:address/schema:addressLocality`,
 3. postal code, `?a/schema:address/schema:postalCode`,
 4. country, `?a/schema:address/schema:addressCountry`.

Also add the Geocoding transformation from the toolbox, transformations-API:

<img src="https://insideout10.github.io/silk-tripadvisor-plugin/images/workbench_step_6.png" />

Connect one by one the source paths to the *geocoding transform*. The order of connection is very important:

 1. street address

<img src="https://insideout10.github.io/silk-tripadvisor-plugin/images/workbench_step_7.png" />

 2. city

<img src="https://insideout10.github.io/silk-tripadvisor-plugin/images/workbench_step_8.png" />

 3. postal code

<img src="https://insideout10.github.io/silk-tripadvisor-plugin/images/workbench_step_9.png" />

Add the source paths, one by one, in our example:

  4. country

<img src="https://insideout10.github.io/silk-tripadvisor-plugin/images/workbench_step_10.png" />

Set the *geocoding transform* parameters. A [MapQuest](http://mapquest.com) key is required, you can get one on the
[MapQuest Developers portal](http://developer.mapquest.com/):

<img src="https://insideout10.github.io/silk-tripadvisor-plugin/images/workbench_step_11.png" />

Add the POI name source path to the editor, in our example `?a/rdfs:label[@lang = 'en']`:

<img src="https://insideout10.github.io/silk-tripadvisor-plugin/images/workbench_step_12.png" />

Add the *TripAdvisor transform* and connect the *geocoding transform* output to it:

<img src="https://insideout10.github.io/silk-tripadvisor-plugin/images/workbench_step_13.png" />

Then connect the name source path to the *TripAdvisor transform*:

<img src="https://insideout10.github.io/silk-tripadvisor-plugin/images/workbench_step_14.png" />

Configure the *TripAdvisor transform* parameters:

 * *appKey*, the application key you received from TripAdvisor,
 * *prefix*, the prefix to use to build the TripAdvisor location address (suggested: http://tripadvisor.com/),
 * *limit*, the maximum number of results.

<img src="https://insideout10.github.io/silk-tripadvisor-plugin/images/workbench_step_15.png" />

This is how the editor should look like at the end:

<img src="https://insideout10.github.io/silk-tripadvisor-plugin/images/workbench_step_16.png" />

Click on **Evaluate** to launch the process and see the results. This is an example result:

<img src="https://insideout10.github.io/silk-tripadvisor-plugin/images/workbench_step_results.png" />


Be aware that TripAdvisor does not allow more than 100 requests per second. Check in the console log for any messages.


### Using the project configuration

Edit or create the `rules.xml` file in Silk's project folder (`~/.silk/workspace/project-name/transform/transform-name/rules.xml`).

The following will load the *TripAdvisor* plugin and will match TripAdvisor data using the coordinates and POI name.
It'll return the TripAdvisor location URL, such as _http://tripadvisor.com/xyz_.

The order of the `Input` elements *is important*, they must be listed in the following order:

 1. latitude/longitude coordinates separated by a comma (e.g. `1.234,2.345`),
 2. POI name


    <TransformRules>
      <TransformRule name="tripadvisor__transformation" targetProperty="&lt;http://schema.org/sameAs&gt;">
        <TransformInput id="tripadvisor" function="tripadvisor">
          <TransformInput id="tripadvisor__geocoding" function="geocoding">
            <Input id="tripadvisor__street" path="?a/&lt;http://schema.org/address&gt;/&lt;http://schema.org/streetAddress&gt;"/>
            <Input id="tripadvisor__city" path="?a/&lt;http://schema.org/address&gt;/&lt;http://schema.org/addressLocality&gt;"/>
            <Input id="tripadvisor__postal-code" path="?a/&lt;http://schema.org/address&gt;/&lt;http://schema.org/postalCode&gt;"/>
            <Input id="tripadvisor__country" path="?a/&lt;http://schema.org/address&gt;/&lt;http://schema.org/addressCountry&gt;"/>
            <Param name="appKey" value="your-mapquest-app-key"/>
            <Param name="limit" value="1"/>
          </TransformInput>
          <Input id="tripadvisor__name-en" path="?a/&lt;http://www.w3.org/2000/01/rdf-schema#label&gt;[@lang = 'en']"/>
          <Param name="appKey" value="your-tripadvisor-app-key"/>
          <Param name="prefix" value="http://tripadvisor.com/"/>
          <Param name="limit" value="1"/>
        </TransformInput>
      </TransformRule>
    </TransformRules>



### Using Single Machine

Following is an example of a `Transform` element that can be configured in a [Silk LSL](https://www.assembla.com/wiki/show/silk/Link_Specification_Language)
configuration file for use with Single Machine.

The order of the `Input` elements *is important*, they must be listed in the following order:

 1. latitude/longitude coordinates separated by a comma (e.g. `1.234,2.345`),
 2. POI name


*Note that for this to work, Silk framework needs [pull request 29](https://github.com/silk-framework/silk/pull/29)*


    <Silk>

      <!-- Your existing configuration ... -->

      <Transforms>
        <Transform id="reconcile-with-tripadvisor">
            <SourceDataset dataSource="source" var="a">
                <RestrictTo>{ ?a a &lt;http://schema.org/LocalBusiness&gt; . } .</RestrictTo>
            </SourceDataset>

            <TransformRule name="TripAdvisor" targetProperty="&lt;http://schema.org/sameAs&gt;">
                <TransformInput id="tripadvisor" function="tripadvisor">
                    <TransformInput id="geocoding" function="geocoding">
                        <Input id="geocoding__address__streetaddress"
                               path="?a/&lt;http://schema.org/address&gt;/&lt;http://schema.org/streetAddress&gt;"/>
                        <Input id="geocoding__address__postalcode"
                               path="?a/&lt;http://schema.org/address&gt;/&lt;http://schema.org/postalCode&gt;"/>
                        <Input id="geocoding__address__addresslocality"
                               path="?a/&lt;http://schema.org/address&gt;/&lt;http://schema.org/addressLocality&gt;"/>
                        <Input id="geocoding__address__addresscountry"
                               path="?a/&lt;http://schema.org/address&gt;/&lt;http://schema.org/addressCountry&gt;"/>
                        <Param name="appKey" value="your-mapquest-app-key"/>
                        <Param name="limit" value="1"/>
                    </TransformInput>
                    <Input id="tripadvisor__label" path="?a/&lt;http://www.w3.org/2000/01/rdf-schema#label&gt;[@lang = 'en']"/>
                    <Param name="appKey" value="your-tripadvisor-app-key"/>
                    <Param name="prefix" value="http://tripadvisor.com/"/>
                    <Param name="limit" value="1"/>
                </TransformInput>
            </TransformRule>

            <Outputs>
                <!-- your existing outputs configuration ... -->
            </Outputs>
        </Transform>
      </Transforms>

    </Silk>


## Acknowledgements

This work is made possible thanks to:

 * [Salzburgerland Tourismus](http://salzburgerland.com) for innovation in Tourism and Linked Data,
 * [Redlink](http://redlink.co) for the Linked Data cloud platform,
 * [WordLift](http://join.wordlift.it) for bring Artificial Intelligence to web publishers,
 * [InsideOut10](http://insideout.io) for the project management and development.

A special thanks goes to:

 * [JetBrains](http://jetbrains.com) for providing the IntelliJ IDEA open source license,
 * [TripAdvisor](http://tripadvisor.com) for providing the Content API used to map locations.
