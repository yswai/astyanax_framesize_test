package com.swyep.cassandra.astyanax.demo.dao;

import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.OperationResult;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.model.Column;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.ColumnList;
import com.netflix.astyanax.query.RowQuery;
import com.netflix.astyanax.recipes.reader.AllRowsReader;
import com.netflix.astyanax.serializers.AnnotatedCompositeSerializer;
import com.netflix.astyanax.serializers.StringSerializer;
import com.swyep.cassandra.astyanax.demo.keys.TargetToDayToCountryToCidCk;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.swyep.cassandra.astyanax.demo.Constants.*;

public class SampleDaoTest {

    private AstyanaxConnector astyanaxConnector;
    private AnnotatedCompositeSerializer<TargetToDayToCountryToCidCk> targetToDayToCountryToCidCkSerializer =
            new AnnotatedCompositeSerializer<>(TargetToDayToCountryToCidCk.class);

    @Before
    public void setUp() throws ConnectionException {
        this.astyanaxConnector = new AstyanaxConnector();
    }

    @Test
    public void testCase1() throws ConnectionException {
        Keyspace keyspace = astyanaxConnector.getKeyspace();
        ColumnFamily<String, String> CF_STANDARD1 = ColumnFamily
                .newColumnFamily("Standard1", StringSerializer.get(), StringSerializer.get());
        Map<String, Object> properties = new HashMap<>();
//        keyspace.createColumnFamily(CF_STANDARD1, properties);

        ColumnFamily<String, TargetToDayToCountryToCidCk> CF_CM_IC = ColumnFamily
                .newColumnFamily("CM_IC_TargetToDayToCountryToCid",
                        StringSerializer.get(),
                        targetToDayToCountryToCidCkSerializer
                );

        TargetToDayToCountryToCidCk START = new TargetToDayToCountryToCidCk(" ", " ", " ", " ");
        TargetToDayToCountryToCidCk END = new TargetToDayToCountryToCidCk(UUID_END, COUNTRY_END, UUID_END, TIMESTAMP_END);

        RowQuery<String, TargetToDayToCountryToCidCk> rowQuery = keyspace.prepareQuery(CF_CM_IC)
                .getRow("20180716")
                .autoPaginate(true)
                .withColumnRange(START, END, false, 1000);

        OperationResult<ColumnList<TargetToDayToCountryToCidCk>> result;

        Map<String, Long> countryCallCount = new ConcurrentHashMap<>();

        int pageNumber = 0;
        int totalRecords = 0;
        while(!(result = rowQuery.execute()).getResult().isEmpty()) {
            pageNumber++;
            System.out.println("------------------------- Page " + pageNumber + " -----------------------------");
            ColumnList<TargetToDayToCountryToCidCk> columns = result.getResult();
            Iterator<Column<TargetToDayToCountryToCidCk>> iterator = columns.iterator();
            while (iterator.hasNext()) {
                totalRecords++;
                Column<TargetToDayToCountryToCidCk> column = iterator.next();
                TargetToDayToCountryToCidCk targetToDayToCountryToCidCk = column.getName();
                System.out.println("Name: " + targetToDayToCountryToCidCk.toString() + ", value: " + column.getStringValue());
                countryCallCount.merge(targetToDayToCountryToCidCk.getCountry(), 1L, (oldVal, newVal) -> Long.valueOf(++oldVal));
            }
        }
        System.out.println("Total pages = " + pageNumber);
        System.out.println("Total records = " + totalRecords);
        System.out.println("------------------------- END -----------------------------");
        System.out.println("");

        for (Map.Entry<String, Long> entry : countryCallCount.entrySet()) {
            System.out.println("Country : " + entry.getKey() + ", Count: " + entry.getValue());
        }
    }

    @Test
    public void fillWithHugeJson() throws Exception {
        Keyspace keyspace = astyanaxConnector.getKeyspace();

        TargetToDayToCountryToCidCk START = new TargetToDayToCountryToCidCk(" ", " ", " ", " ");
        TargetToDayToCountryToCidCk END = new TargetToDayToCountryToCidCk(UUID_END, COUNTRY_END, UUID_END, TIMESTAMP_END);

        Set<String> rowKeys = new HashSet<>();

        ColumnFamily<String, TargetToDayToCountryToCidCk> CF_CM_IC = ColumnFamily
                .newColumnFamily("CM_IC_TargetToDayToCountryToCid",
                        StringSerializer.get(),
                        targetToDayToCountryToCidCkSerializer
                );
        new AllRowsReader.Builder<>(keyspace, CF_CM_IC)
                .withColumnRange(null, null, false, 0)
                .withPartitioner(null) // this will use keyspace's partitioner
                .forEachRow(row -> {
                    rowKeys.add(row.getKey());
                    return true;
                })
                .build()
                .call();

        for (String rowKey : rowKeys) {
            RowQuery<String, TargetToDayToCountryToCidCk> rowQuery = keyspace.prepareQuery(CF_CM_IC)
                    .getRow(rowKey)
                    .autoPaginate(true)
                    .withColumnRange(START, END, false, 100);

            OperationResult<ColumnList<TargetToDayToCountryToCidCk>> result;

            while(!(result = rowQuery.execute()).getResult().isEmpty()) {
                ColumnList<TargetToDayToCountryToCidCk> columns = result.getResult();
                Iterator<Column<TargetToDayToCountryToCidCk>> iterator = columns.iterator();
                while (iterator.hasNext()) {
                    TargetToDayToCountryToCidCk targetToDayToCountryToCidCk = iterator.next().getName();
                    keyspace.prepareColumnMutation(CF_CM_IC, rowKey, targetToDayToCountryToCidCk)
                            .putValue(json, null).execute();
                }
            }
        }
    }

    String json = "[\t" +
            "  {\t" +
            "    \"_id\": \"5b5477f77fb16ffeb518bec7\",\t" +
            "    \"index\": 0,\t" +
            "    \"guid\": \"be3d8dfe-c917-47db-ab60-615d06d34e9e\",\t" +
            "    \"isActive\": false,\t" +
            "    \"balance\": \"$1,653.25\",\t" +
            "    \"picture\": \"http://placehold.it/32x32\",\t" +
            "    \"age\": 24,\t" +
            "    \"eyeColor\": \"green\",\t" +
            "    \"name\": \"Marks Edwards\",\t" +
            "    \"gender\": \"male\",\t" +
            "    \"company\": \"COGNICODE\",\t" +
            "    \"email\": \"marksedwards@cognicode.com\",\t" +
            "    \"phone\": \"+1 (888) 471-3743\",\t" +
            "    \"address\": \"231 Veterans Avenue, Bath, Maryland, 8869\",\t" +
            "    \"about\": \"Id sint eiusmod ullamco tempor quis occaecat commodo consectetur magna ea velit. Irure et proident aute ad ad laborum elit ea. Veniam dolor ipsum sunt dolor est elit amet esse nulla. Fugiat aliqua et velit aute aliquip ullamco dolore sunt consequat Lorem ex magna.\\r\\t\",\t" +
            "    \"registered\": \"2017-09-04T01:22:07 -08:00\",\t" +
            "    \"latitude\": -81.165806,\t" +
            "    \"longitude\": 87.276099,\t" +
            "    \"tags\": [\t" +
            "      \"minim\",\t" +
            "      \"nostrud\",\t" +
            "      \"pariatur\",\t" +
            "      \"esse\",\t" +
            "      \"quis\",\t" +
            "      \"anim\",\t" +
            "      \"labore\"\t" +
            "    ],\t" +
            "    \"friends\": [\t" +
            "      {\t" +
            "        \"id\": 0,\t" +
            "        \"name\": \"Lindsay Kidd\"\t" +
            "      },\t" +
            "      {\t" +
            "        \"id\": 1,\t" +
            "        \"name\": \"Mabel Wilkinson\"\t" +
            "      },\t" +
            "      {\t" +
            "        \"id\": 2,\t" +
            "        \"name\": \"Brandy Park\"\t" +
            "      }\t" +
            "    ],\t" +
            "    \"greeting\": \"Hello, Marks Edwards! You have 10 unread messages.\",\t" +
            "    \"favoriteFruit\": \"banana\"\t" +
            "  },\t" +
            "  {\t" +
            "    \"_id\": \"5b5477f7374f1a41bd4f8079\",\t" +
            "    \"index\": 1,\t" +
            "    \"guid\": \"9fce5498-8aa7-4118-b3ae-1179b6c251d3\",\t" +
            "    \"isActive\": true,\t" +
            "    \"balance\": \"$1,556.63\",\t" +
            "    \"picture\": \"http://placehold.it/32x32\",\t" +
            "    \"age\": 36,\t" +
            "    \"eyeColor\": \"green\",\t" +
            "    \"name\": \"Chapman Jefferson\",\t" +
            "    \"gender\": \"male\",\t" +
            "    \"company\": \"ORBIXTAR\",\t" +
            "    \"email\": \"chapmanjefferson@orbixtar.com\",\t" +
            "    \"phone\": \"+1 (831) 462-2135\",\t" +
            "    \"address\": \"457 Hastings Street, Hasty, Florida, 5523\",\t" +
            "    \"about\": \"Magna commodo veniam fugiat commodo magna ea cillum laboris minim in Lorem ut incididunt. Ea sunt pariatur ut sit non et voluptate non dolor nisi nisi enim fugiat anim. Minim id irure quis ex labore. Commodo officia in culpa eiusmod consectetur adipisicing.\\r\\t\",\t" +
            "    \"registered\": \"2014-06-14T09:39:21 -08:00\",\t" +
            "    \"latitude\": 87.823998,\t" +
            "    \"longitude\": -63.256722,\t" +
            "    \"tags\": [\t" +
            "      \"non\",\t" +
            "      \"ex\",\t" +
            "      \"incididunt\",\t" +
            "      \"consequat\",\t" +
            "      \"sint\",\t" +
            "      \"consequat\",\t" +
            "      \"id\"\t" +
            "    ],\t" +
            "    \"friends\": [\t" +
            "      {\t" +
            "        \"id\": 0,\t" +
            "        \"name\": \"Hester Blackwell\"\t" +
            "      },\t" +
            "      {\t" +
            "        \"id\": 1,\t" +
            "        \"name\": \"Adela Mendoza\"\t" +
            "      },\t" +
            "      {\t" +
            "        \"id\": 2,\t" +
            "        \"name\": \"Roseann Stout\"\t" +
            "      }\t" +
            "    ],\t" +
            "    \"greeting\": \"Hello, Chapman Jefferson! You have 1 unread messages.\",\t" +
            "    \"favoriteFruit\": \"banana\"\t" +
            "  },\t" +
            "  {\t" +
            "    \"_id\": \"5b5477f729195bbad095512b\",\t" +
            "    \"index\": 2,\t" +
            "    \"guid\": \"fe8097fb-4bc9-429a-84a4-eb5abc600abb\",\t" +
            "    \"isActive\": true,\t" +
            "    \"balance\": \"$1,253.54\",\t" +
            "    \"picture\": \"http://placehold.it/32x32\",\t" +
            "    \"age\": 33,\t" +
            "    \"eyeColor\": \"brown\",\t" +
            "    \"name\": \"Cross Jackson\",\t" +
            "    \"gender\": \"male\",\t" +
            "    \"company\": \"CALCU\",\t" +
            "    \"email\": \"crossjackson@calcu.com\",\t" +
            "    \"phone\": \"+1 (906) 569-2630\",\t" +
            "    \"address\": \"442 Osborn Street, Carlton, Pennsylvania, 5903\",\t" +
            "    \"about\": \"Voluptate veniam mollit dolore sunt sunt cillum elit consectetur cupidatat. Proident quis mollit nostrud magna est do esse. Magna deserunt adipisicing duis duis eiusmod est quis reprehenderit cupidatat consectetur. Velit tempor velit laboris nisi anim qui adipisicing culpa tempor elit.\\r\\t\",\t" +
            "    \"registered\": \"2014-10-29T08:13:35 -08:00\",\t" +
            "    \"latitude\": -69.41504,\t" +
            "    \"longitude\": -119.400786,\t" +
            "    \"tags\": [\t" +
            "      \"ullamco\",\t" +
            "      \"consectetur\",\t" +
            "      \"ullamco\",\t" +
            "      \"magna\",\t" +
            "      \"in\",\t" +
            "      \"anim\",\t" +
            "      \"enim\"\t" +
            "    ],\t" +
            "    \"friends\": [\t" +
            "      {\t" +
            "        \"id\": 0,\t" +
            "        \"name\": \"Hopkins Higgins\"\t" +
            "      },\t" +
            "      {\t" +
            "        \"id\": 1,\t" +
            "        \"name\": \"Christi Diaz\"\t" +
            "      },\t" +
            "      {\t" +
            "        \"id\": 2,\t" +
            "        \"name\": \"Paula Cummings\"\t" +
            "      }\t" +
            "    ],\t" +
            "    \"greeting\": \"Hello, Cross Jackson! You have 4 unread messages.\",\t" +
            "    \"favoriteFruit\": \"strawberry\"\t" +
            "  },\t" +
            "  {\t" +
            "    \"_id\": \"5b5477f72e434eb96a5b6501\",\t" +
            "    \"index\": 3,\t" +
            "    \"guid\": \"871f260e-b681-4867-9595-1220ef1cefe0\",\t" +
            "    \"isActive\": true,\t" +
            "    \"balance\": \"$1,403.86\",\t" +
            "    \"picture\": \"http://placehold.it/32x32\",\t" +
            "    \"age\": 27,\t" +
            "    \"eyeColor\": \"brown\",\t" +
            "    \"name\": \"Nell Blanchard\",\t" +
            "    \"gender\": \"female\",\t" +
            "    \"company\": \"PETIGEMS\",\t" +
            "    \"email\": \"nellblanchard@petigems.com\",\t" +
            "    \"phone\": \"+1 (955) 524-3090\",\t" +
            "    \"address\": \"318 Dahl Court, Chemung, District Of Columbia, 3058\",\t" +
            "    \"about\": \"Sunt pariatur irure nisi aliquip officia fugiat. Tempor cillum pariatur voluptate ullamco minim aliqua incididunt do tempor qui tempor mollit esse exercitation. Occaecat officia fugiat cupidatat reprehenderit veniam irure incididunt ex dolore fugiat eiusmod nulla excepteur. Exercitation anim dolor proident in proident labore exercitation incididunt amet duis exercitation esse. Tempor non do cillum proident eu anim ex nisi minim officia.\\r\\t\",\t" +
            "    \"registered\": \"2014-04-11T10:11:29 -08:00\",\t" +
            "    \"latitude\": 87.546494,\t" +
            "    \"longitude\": -64.826306,\t" +
            "    \"tags\": [\t" +
            "      \"eiusmod\",\t" +
            "      \"consectetur\",\t" +
            "      \"aliquip\",\t" +
            "      \"adipisicing\",\t" +
            "      \"aute\",\t" +
            "      \"occaecat\",\t" +
            "      \"ullamco\"\t" +
            "    ],\t" +
            "    \"friends\": [\t" +
            "      {\t" +
            "        \"id\": 0,\t" +
            "        \"name\": \"Fulton Price\"\t" +
            "      },\t" +
            "      {\t" +
            "        \"id\": 1,\t" +
            "        \"name\": \"Willa Knight\"\t" +
            "      },\t" +
            "      {\t" +
            "        \"id\": 2,\t" +
            "        \"name\": \"Josefina Anderson\"\t" +
            "      }\t" +
            "    ],\t" +
            "    \"greeting\": \"Hello, Nell Blanchard! You have 2 unread messages.\",\t" +
            "    \"favoriteFruit\": \"strawberry\"\t" +
            "  },\t" +
            "  {\t" +
            "    \"_id\": \"5b5477f7c2d312a6baa283e3\",\t" +
            "    \"index\": 4,\t" +
            "    \"guid\": \"7706d06e-8b7b-460d-93ad-c6598beda57e\",\t" +
            "    \"isActive\": true,\t" +
            "    \"balance\": \"$2,726.49\",\t" +
            "    \"picture\": \"http://placehold.it/32x32\",\t" +
            "    \"age\": 25,\t" +
            "    \"eyeColor\": \"blue\",\t" +
            "    \"name\": \"Daugherty Fischer\",\t" +
            "    \"gender\": \"male\",\t" +
            "    \"company\": \"OTHERSIDE\",\t" +
            "    \"email\": \"daughertyfischer@otherside.com\",\t" +
            "    \"phone\": \"+1 (953) 495-2386\",\t" +
            "    \"address\": \"990 Dorchester Road, Mathews, New Hampshire, 5687\",\t" +
            "    \"about\": \"Cillum consectetur eiusmod do eiusmod excepteur exercitation laborum. Excepteur fugiat id consectetur velit aute in deserunt irure consectetur mollit labore esse amet. Dolor ex mollit enim est.\\r\\t\",\t" +
            "    \"registered\": \"2015-08-08T06:19:30 -08:00\",\t" +
            "    \"latitude\": 17.770914,\t" +
            "    \"longitude\": -37.351943,\t" +
            "    \"tags\": [\t" +
            "      \"incididunt\",\t" +
            "      \"elit\",\t" +
            "      \"nisi\",\t" +
            "      \"fugiat\",\t" +
            "      \"irure\",\t" +
            "      \"ipsum\",\t" +
            "      \"anim\"\t" +
            "    ],\t" +
            "    \"friends\": [\t" +
            "      {\t" +
            "        \"id\": 0,\t" +
            "        \"name\": \"White Dotson\"\t" +
            "      },\t" +
            "      {\t" +
            "        \"id\": 1,\t" +
            "        \"name\": \"Nash Acosta\"\t" +
            "      },\t" +
            "      {\t" +
            "        \"id\": 2,\t" +
            "        \"name\": \"Fuller Mckenzie\"\t" +
            "      }\t" +
            "    ],\t" +
            "    \"greeting\": \"Hello, Daugherty Fischer! You have 2 unread messages.\",\t" +
            "    \"favoriteFruit\": \"banana\"\t" +
            "  },\t" +
            "  {\t" +
            "    \"_id\": \"5b5477f7ae6eb0ccb7b59c42\",\t" +
            "    \"index\": 5,\t" +
            "    \"guid\": \"89f8c931-e526-458f-8548-04549fc6cd4f\",\t" +
            "    \"isActive\": true,\t" +
            "    \"balance\": \"$2,877.62\",\t" +
            "    \"picture\": \"http://placehold.it/32x32\",\t" +
            "    \"age\": 28,\t" +
            "    \"eyeColor\": \"green\",\t" +
            "    \"name\": \"Melinda Hoffman\",\t" +
            "    \"gender\": \"female\",\t" +
            "    \"company\": \"IPLAX\",\t" +
            "    \"email\": \"melindahoffman@iplax.com\",\t" +
            "    \"phone\": \"+1 (848) 431-3196\",\t" +
            "    \"address\": \"613 Linden Boulevard, Manila, New Jersey, 5294\",\t" +
            "    \"about\": \"Deserunt cillum cupidatat enim id excepteur. Mollit culpa commodo aliqua non et velit. Cillum proident excepteur velit ut Lorem culpa. Duis enim culpa qui voluptate sunt aute. Laborum minim duis ullamco qui laboris laboris minim commodo. Ullamco incididunt excepteur duis nisi mollit aliquip qui cupidatat dolore et aliqua occaecat. Officia aute id minim magna officia dolor commodo quis adipisicing dolore minim minim.\\r\\t\",\t" +
            "    \"registered\": \"2016-08-30T12:39:34 -08:00\",\t" +
            "    \"latitude\": 22.830109,\t" +
            "    \"longitude\": -150.706203,\t" +
            "    \"tags\": [\t" +
            "      \"exercitation\",\t" +
            "      \"sit\",\t" +
            "      \"nisi\",\t" +
            "      \"eu\",\t" +
            "      \"quis\",\t" +
            "      \"proident\",\t" +
            "      \"consectetur\"\t" +
            "    ],\t" +
            "    \"friends\": [\t" +
            "      {\t" +
            "        \"id\": 0,\t" +
            "        \"name\": \"Chasity Reeves\"\t" +
            "      },\t" +
            "      {\t" +
            "        \"id\": 1,\t" +
            "        \"name\": \"Lula Wood\"\t" +
            "      },\t" +
            "      {\t" +
            "        \"id\": 2,\t" +
            "        \"name\": \"Katharine Garza\"\t" +
            "      }\t" +
            "    ],\t" +
            "    \"greeting\": \"Hello, Melinda Hoffman! You have 6 unread messages.\",\t" +
            "    \"favoriteFruit\": \"banana\"\t" +
            "  },\t" +
            "  {\t" +
            "    \"_id\": \"5b5477f7a4a33c69c2955116\",\t" +
            "    \"index\": 6,\t" +
            "    \"guid\": \"0687d6a9-b60a-4e27-bd86-a15a46611bcf\",\t" +
            "    \"isActive\": false,\t" +
            "    \"balance\": \"$2,769.11\",\t" +
            "    \"picture\": \"http://placehold.it/32x32\",\t" +
            "    \"age\": 36,\t" +
            "    \"eyeColor\": \"green\",\t" +
            "    \"name\": \"Rosalyn Beck\",\t" +
            "    \"gender\": \"female\",\t" +
            "    \"company\": \"MUSANPOLY\",\t" +
            "    \"email\": \"rosalynbeck@musanpoly.com\",\t" +
            "    \"phone\": \"+1 (903) 579-3793\",\t" +
            "    \"address\": \"741 Howard Avenue, Wescosville, Washington, 7630\",\t" +
            "    \"about\": \"Sunt labore ullamco eiusmod tempor deserunt ullamco aliqua est mollit laboris. Amet ullamco dolore ex Lorem eiusmod anim et mollit anim dolore proident dolor nostrud. Aliquip commodo quis qui ipsum adipisicing.\\r\\t\",\t" +
            "    \"registered\": \"2016-10-16T12:03:26 -08:00\",\t" +
            "    \"latitude\": -83.790445,\t" +
            "    \"longitude\": -12.546483,\t" +
            "    \"tags\": [\t" +
            "      \"excepteur\",\t" +
            "      \"magna\",\t" +
            "      \"minim\",\t" +
            "      \"sit\",\t" +
            "      \"labore\",\t" +
            "      \"ea\",\t" +
            "      \"labore\"\t" +
            "    ],\t" +
            "    \"friends\": [\t" +
            "      {\t" +
            "        \"id\": 0,\t" +
            "        \"name\": \"Jami Whitley\"\t" +
            "      },\t" +
            "      {\t" +
            "        \"id\": 1,\t" +
            "        \"name\": \"Salas Hamilton\"\t" +
            "      },\t" +
            "      {\t" +
            "        \"id\": 2,\t" +
            "        \"name\": \"Mcbride Walters\"\t" +
            "      }\t" +
            "    ],\t" +
            "    \"greeting\": \"Hello, Rosalyn Beck! You have 10 unread messages.\",\t" +
            "    \"favoriteFruit\": \"banana\"\t" +
            "  },\t" +
            "  {\t" +
            "    \"_id\": \"5b5477f7e634fcaad73adf89\",\t" +
            "    \"index\": 7,\t" +
            "    \"guid\": \"1f847298-1629-400f-9380-164fb7efe740\",\t" +
            "    \"isActive\": false,\t" +
            "    \"balance\": \"$2,965.24\",\t" +
            "    \"picture\": \"http://placehold.it/32x32\",\t" +
            "    \"age\": 36,\t" +
            "    \"eyeColor\": \"brown\",\t" +
            "    \"name\": \"Susan Farmer\",\t" +
            "    \"gender\": \"female\",\t" +
            "    \"company\": \"ECRATER\",\t" +
            "    \"email\": \"susanfarmer@ecrater.com\",\t" +
            "    \"phone\": \"+1 (997) 461-2069\",\t" +
            "    \"address\": \"825 Hoyts Lane, Hiko, Oregon, 3567\",\t" +
            "    \"about\": \"Quis nostrud irure nostrud nisi id esse ad elit est nisi. Ex deserunt consequat pariatur nostrud dolor Lorem aliquip duis incididunt nostrud adipisicing reprehenderit. Eu duis sit labore laboris dolore sint. Consequat eiusmod sunt eiusmod non enim cupidatat veniam veniam ex. Aute do eiusmod et excepteur non eiusmod in anim sunt.\\r\\t\",\t" +
            "    \"registered\": \"2017-10-07T08:42:02 -08:00\",\t" +
            "    \"latitude\": -46.614505,\t" +
            "    \"longitude\": 117.788998,\t" +
            "    \"tags\": [\t" +
            "      \"id\",\t" +
            "      \"ullamco\",\t" +
            "      \"tempor\",\t" +
            "      \"dolore\",\t" +
            "      \"elit\",\t" +
            "      \"aliquip\",\t" +
            "      \"nostrud\"\t" +
            "    ],\t" +
            "    \"friends\": [\t" +
            "      {\t" +
            "        \"id\": 0,\t" +
            "        \"name\": \"Mayo Goodman\"\t" +
            "      },\t" +
            "      {\t" +
            "        \"id\": 1,\t" +
            "        \"name\": \"Price Brock\"\t" +
            "      },\t" +
            "      {\t" +
            "        \"id\": 2,\t" +
            "        \"name\": \"Genevieve Bryan\"\t" +
            "      }\t" +
            "    ],\t" +
            "    \"greeting\": \"Hello, Susan Farmer! You have 2 unread messages.\",\t" +
            "    \"favoriteFruit\": \"strawberry\"\t" +
            "  }\t" +
            "]";
}
