package pl.edu.zut.mad.appwizut2.network;

import android.os.Handler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

import pl.edu.zut.mad.appwizut2.models.Timetable;

/**
 * Helper class for loading schedule
 */
public class ScheduleLoader {
    /**
     * Schedule we use for testing
     * TODO: Download these from server (and cache)
     */
    private static final String TEST_SCHEDULE = "{\n" +
            "    \"hours\": [\n" +
            "        [\n" +
            "            [\n" +
            "                8, \n" +
            "                15\n" +
            "            ], \n" +
            "            [\n" +
            "                9, \n" +
            "                0\n" +
            "            ]\n" +
            "        ], \n" +
            "        [\n" +
            "            [\n" +
            "                9, \n" +
            "                15\n" +
            "            ], \n" +
            "            [\n" +
            "                10, \n" +
            "                0\n" +
            "            ]\n" +
            "        ], \n" +
            "        [\n" +
            "            [\n" +
            "                10, \n" +
            "                15\n" +
            "            ], \n" +
            "            [\n" +
            "                11, \n" +
            "                0\n" +
            "            ]\n" +
            "        ], \n" +
            "        [\n" +
            "            [\n" +
            "                11, \n" +
            "                15\n" +
            "            ], \n" +
            "            [\n" +
            "                12, \n" +
            "                0\n" +
            "            ]\n" +
            "        ], \n" +
            "        [\n" +
            "            [\n" +
            "                12, \n" +
            "                15\n" +
            "            ], \n" +
            "            [\n" +
            "                13, \n" +
            "                0\n" +
            "            ]\n" +
            "        ], \n" +
            "        [\n" +
            "            [\n" +
            "                13, \n" +
            "                15\n" +
            "            ], \n" +
            "            [\n" +
            "                14, \n" +
            "                0\n" +
            "            ]\n" +
            "        ], \n" +
            "        [\n" +
            "            [\n" +
            "                14, \n" +
            "                15\n" +
            "            ], \n" +
            "            [\n" +
            "                15, \n" +
            "                0\n" +
            "            ]\n" +
            "        ], \n" +
            "        [\n" +
            "            [\n" +
            "                15, \n" +
            "                15\n" +
            "            ], \n" +
            "            [\n" +
            "                16, \n" +
            "                0\n" +
            "            ]\n" +
            "        ], \n" +
            "        [\n" +
            "            [\n" +
            "                16, \n" +
            "                15\n" +
            "            ], \n" +
            "            [\n" +
            "                17, \n" +
            "                0\n" +
            "            ]\n" +
            "        ], \n" +
            "        [\n" +
            "            [\n" +
            "                17, \n" +
            "                15\n" +
            "            ], \n" +
            "            [\n" +
            "                18, \n" +
            "                0\n" +
            "            ]\n" +
            "        ], \n" +
            "        [\n" +
            "            [\n" +
            "                18, \n" +
            "                15\n" +
            "            ], \n" +
            "            [\n" +
            "                19, \n" +
            "                0\n" +
            "            ]\n" +
            "        ], \n" +
            "        [\n" +
            "            [\n" +
            "                19, \n" +
            "                15\n" +
            "            ], \n" +
            "            [\n" +
            "                20, \n" +
            "                0\n" +
            "            ]\n" +
            "        ], \n" +
            "        [\n" +
            "            [\n" +
            "                20, \n" +
            "                15\n" +
            "            ], \n" +
            "            [\n" +
            "                21, \n" +
            "                0\n" +
            "            ]\n" +
            "        ]\n" +
            "    ], \n" +
            "    \"weekdaySchedules\": [\n" +
            "        [\n" +
            "            [\n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"Fizyka\", \n" +
            "                        \"dr M.Lewandowska\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"C\", \n" +
            "                        \"BMW 14\"\n" +
            "                    ], \n" +
            "                    \"name\": \"Fizyka\", \n" +
            "                    \"rawWG\": null, \n" +
            "                    \"room\": \"BMW 14\", \n" +
            "                    \"teacher\": \"dr M.Lewandowska\", \n" +
            "                    \"type\": \"C\"\n" +
            "                }\n" +
            "            ], \n" +
            "            [], \n" +
            "            [\n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"Elementy cyfrowe i uk\\u0142ady logiczne\", \n" +
            "                        \"Ty.N\", \n" +
            "                        \"dr G.Cariowa\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"C\", \n" +
            "                        \"WI2- 14\"\n" +
            "                    ], \n" +
            "                    \"name\": \"Elementy cyfrowe i uk\\u0142ady logiczne\", \n" +
            "                    \"rawWG\": \"Ty.N\", \n" +
            "                    \"room\": \"WI2- 14\", \n" +
            "                    \"teacher\": \"dr G.Cariowa\", \n" +
            "                    \"type\": \"C\"\n" +
            "                }, \n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"Fizyka\", \n" +
            "                        \"gr.B Ty.P\", \n" +
            "                        \"dr T.Bodziony\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"L\", \n" +
            "                        \"BMW 603\"\n" +
            "                    ], \n" +
            "                    \"name\": \"Fizyka\", \n" +
            "                    \"rawWG\": \"gr.B Ty.P\", \n" +
            "                    \"room\": \"BMW 603\", \n" +
            "                    \"teacher\": \"dr T.Bodziony\", \n" +
            "                    \"type\": \"L\"\n" +
            "                }, \n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"Fizyka\", \n" +
            "                        \"gr.A Ty.P\", \n" +
            "                        \"dr M.Lewandowska\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"L\", \n" +
            "                        \"BMW 603\"\n" +
            "                    ], \n" +
            "                    \"name\": \"Fizyka\", \n" +
            "                    \"rawWG\": \"gr.A Ty.P\", \n" +
            "                    \"room\": \"BMW 603\", \n" +
            "                    \"teacher\": \"dr M.Lewandowska\", \n" +
            "                    \"type\": \"L\"\n" +
            "                }\n" +
            "            ], \n" +
            "            [\n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"Elementy cyfrowe i uk\\u0142ady logiczne\", \n" +
            "                        \"Ty.N\", \n" +
            "                        \"dr G.Cariowa\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"C\", \n" +
            "                        \"WI2- 14\"\n" +
            "                    ], \n" +
            "                    \"name\": \"Elementy cyfrowe i uk\\u0142ady logiczne\", \n" +
            "                    \"rawWG\": \"Ty.N\", \n" +
            "                    \"room\": \"WI2- 14\", \n" +
            "                    \"teacher\": \"dr G.Cariowa\", \n" +
            "                    \"type\": \"C\"\n" +
            "                }, \n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"Fizyka\", \n" +
            "                        \"gr.B Ty.P\", \n" +
            "                        \"dr T.Bodziony\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"L\", \n" +
            "                        \"BMW 603\"\n" +
            "                    ], \n" +
            "                    \"name\": \"Fizyka\", \n" +
            "                    \"rawWG\": \"gr.B Ty.P\", \n" +
            "                    \"room\": \"BMW 603\", \n" +
            "                    \"teacher\": \"dr T.Bodziony\", \n" +
            "                    \"type\": \"L\"\n" +
            "                }, \n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"Fizyka\", \n" +
            "                        \"gr.A Ty.P\", \n" +
            "                        \"dr M.Lewandowska\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"L\", \n" +
            "                        \"BMW 603\"\n" +
            "                    ], \n" +
            "                    \"name\": \"Fizyka\", \n" +
            "                    \"rawWG\": \"gr.A Ty.P\", \n" +
            "                    \"room\": \"BMW 603\", \n" +
            "                    \"teacher\": \"dr M.Lewandowska\", \n" +
            "                    \"type\": \"L\"\n" +
            "                }\n" +
            "            ], \n" +
            "            [], \n" +
            "            [], \n" +
            "            [], \n" +
            "            [], \n" +
            "            [\n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"BHP, ergonomia pracy i ochrona przeciwpo\\u017ca\", \n" +
            "                        \"tylko 16.11.2015r. godz.16:30\", \n" +
            "                        \"dr Z.\\u0141osiewicz\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"W\", \n" +
            "                        \"WI2- 126\"\n" +
            "                    ], \n" +
            "                    \"name\": \"BHP, ergonomia pracy i ochrona przeciwpo\\u017ca\", \n" +
            "                    \"rawWG\": \"tylko 16.11.2015r. godz.16:30\", \n" +
            "                    \"room\": \"WI2- 126\", \n" +
            "                    \"teacher\": \"dr Z.\\u0141osiewicz\", \n" +
            "                    \"type\": \"W\"\n" +
            "                }\n" +
            "            ], \n" +
            "            [\n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"BHP, ergonomia pracy i ochrona przeciwpo\\u017ca\", \n" +
            "                        \"tylko 16.11.2015r. godz.16:30\", \n" +
            "                        \"dr Z.\\u0141osiewicz\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"W\", \n" +
            "                        \"WI2- 126\"\n" +
            "                    ], \n" +
            "                    \"name\": \"BHP, ergonomia pracy i ochrona przeciwpo\\u017ca\", \n" +
            "                    \"rawWG\": \"tylko 16.11.2015r. godz.16:30\", \n" +
            "                    \"room\": \"WI2- 126\", \n" +
            "                    \"teacher\": \"dr Z.\\u0141osiewicz\", \n" +
            "                    \"type\": \"W\"\n" +
            "                }\n" +
            "            ], \n" +
            "            [\n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"BHP, ergonomia pracy i ochrona przeciwpo\\u017ca\", \n" +
            "                        \"tylko 16.11.2015r. godz.16:30\", \n" +
            "                        \"dr Z.\\u0141osiewicz\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"W\", \n" +
            "                        \"WI2- 126\"\n" +
            "                    ], \n" +
            "                    \"name\": \"BHP, ergonomia pracy i ochrona przeciwpo\\u017ca\", \n" +
            "                    \"rawWG\": \"tylko 16.11.2015r. godz.16:30\", \n" +
            "                    \"room\": \"WI2- 126\", \n" +
            "                    \"teacher\": \"dr Z.\\u0141osiewicz\", \n" +
            "                    \"type\": \"W\"\n" +
            "                }\n" +
            "            ], \n" +
            "            [\n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"BHP, ergonomia pracy i ochrona przeciwpo\\u017ca\", \n" +
            "                        \"tylko 16.11.2015r. godz.16:30\", \n" +
            "                        \"dr Z.\\u0141osiewicz\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"W\", \n" +
            "                        \"WI2- 126\"\n" +
            "                    ], \n" +
            "                    \"name\": \"BHP, ergonomia pracy i ochrona przeciwpo\\u017ca\", \n" +
            "                    \"rawWG\": \"tylko 16.11.2015r. godz.16:30\", \n" +
            "                    \"room\": \"WI2- 126\", \n" +
            "                    \"teacher\": \"dr Z.\\u0141osiewicz\", \n" +
            "                    \"type\": \"W\"\n" +
            "                }\n" +
            "            ], \n" +
            "            [\n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"BHP, ergonomia pracy i ochrona przeciwpo\\u017ca\", \n" +
            "                        \"tylko 16.11.2015r. godz.16:30\", \n" +
            "                        \"dr Z.\\u0141osiewicz\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"W\", \n" +
            "                        \"WI2- 126\"\n" +
            "                    ], \n" +
            "                    \"name\": \"BHP, ergonomia pracy i ochrona przeciwpo\\u017ca\", \n" +
            "                    \"rawWG\": \"tylko 16.11.2015r. godz.16:30\", \n" +
            "                    \"room\": \"WI2- 126\", \n" +
            "                    \"teacher\": \"dr Z.\\u0141osiewicz\", \n" +
            "                    \"type\": \"W\"\n" +
            "                }\n" +
            "            ]\n" +
            "        ], \n" +
            "        [\n" +
            "            [], \n" +
            "            [], \n" +
            "            [\n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"Business Hour\", \n" +
            "                        \"SPOTKANIA Z FIRMAMI\", \n" +
            "                        \"-.----------\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"W\", \n" +
            "                        \"WI2- 126\"\n" +
            "                    ], \n" +
            "                    \"name\": \"Business Hour\", \n" +
            "                    \"rawWG\": \"SPOTKANIA Z FIRMAMI\", \n" +
            "                    \"room\": \"WI2- 126\", \n" +
            "                    \"teacher\": \"-.----------\", \n" +
            "                    \"type\": \"W\"\n" +
            "                }\n" +
            "            ], \n" +
            "            [\n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"Business Hour\", \n" +
            "                        \"SPOTKANIA Z FIRMAMI\", \n" +
            "                        \"-.----------\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"W\", \n" +
            "                        \"WI2- 126\"\n" +
            "                    ], \n" +
            "                    \"name\": \"Business Hour\", \n" +
            "                    \"rawWG\": \"SPOTKANIA Z FIRMAMI\", \n" +
            "                    \"room\": \"WI2- 126\", \n" +
            "                    \"teacher\": \"-.----------\", \n" +
            "                    \"type\": \"W\"\n" +
            "                }\n" +
            "            ], \n" +
            "            [\n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"Analiza matematyczna i algebra liniowa I\", \n" +
            "                        \"Ty.P\", \n" +
            "                        \"dr Z.St\\u0119pie\\u0144\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"W\", \n" +
            "                        \"WI2- 126\"\n" +
            "                    ], \n" +
            "                    \"name\": \"Analiza matematyczna i algebra liniowa I\", \n" +
            "                    \"rawWG\": \"Ty.P\", \n" +
            "                    \"room\": \"WI2- 126\", \n" +
            "                    \"teacher\": \"dr Z.St\\u0119pie\\u0144\", \n" +
            "                    \"type\": \"W\"\n" +
            "                }\n" +
            "            ], \n" +
            "            [\n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"Analiza matematyczna i algebra liniowa I\", \n" +
            "                        \"Ty.P\", \n" +
            "                        \"dr Z.St\\u0119pie\\u0144\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"W\", \n" +
            "                        \"WI2- 126\"\n" +
            "                    ], \n" +
            "                    \"name\": \"Analiza matematyczna i algebra liniowa I\", \n" +
            "                    \"rawWG\": \"Ty.P\", \n" +
            "                    \"room\": \"WI2- 126\", \n" +
            "                    \"teacher\": \"dr Z.St\\u0119pie\\u0144\", \n" +
            "                    \"type\": \"W\"\n" +
            "                }\n" +
            "            ], \n" +
            "            [\n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"Elektronika\", \n" +
            "                        \"gr.A,B; A TN, B TP\", \n" +
            "                        \"dr M.Pelczar\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"L\", \n" +
            "                        \"WI2- 221\"\n" +
            "                    ], \n" +
            "                    \"name\": \"Elektronika\", \n" +
            "                    \"rawWG\": \"gr.A,B; A TN, B TP\", \n" +
            "                    \"room\": \"WI2- 221\", \n" +
            "                    \"teacher\": \"dr M.Pelczar\", \n" +
            "                    \"type\": \"L\"\n" +
            "                }\n" +
            "            ], \n" +
            "            [\n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"Elektronika\", \n" +
            "                        \"gr.A,B; A TN, B TP\", \n" +
            "                        \"dr M.Pelczar\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"L\", \n" +
            "                        \"WI2- 221\"\n" +
            "                    ], \n" +
            "                    \"name\": \"Elektronika\", \n" +
            "                    \"rawWG\": \"gr.A,B; A TN, B TP\", \n" +
            "                    \"room\": \"WI2- 221\", \n" +
            "                    \"teacher\": \"dr M.Pelczar\", \n" +
            "                    \"type\": \"L\"\n" +
            "                }\n" +
            "            ], \n" +
            "            [], \n" +
            "            [], \n" +
            "            [], \n" +
            "            [], \n" +
            "            []\n" +
            "        ], \n" +
            "        [\n" +
            "            [\n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"Fizyka\", \n" +
            "                        \"dr T.Bodziony\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"W\", \n" +
            "                        \"WM 301\"\n" +
            "                    ], \n" +
            "                    \"name\": \"Fizyka\", \n" +
            "                    \"rawWG\": null, \n" +
            "                    \"room\": \"WM 301\", \n" +
            "                    \"teacher\": \"dr T.Bodziony\", \n" +
            "                    \"type\": \"W\"\n" +
            "                }\n" +
            "            ], \n" +
            "            [\n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"Fizyka\", \n" +
            "                        \"dr T.Bodziony\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"W\", \n" +
            "                        \"WM 301\"\n" +
            "                    ], \n" +
            "                    \"name\": \"Fizyka\", \n" +
            "                    \"rawWG\": null, \n" +
            "                    \"room\": \"WM 301\", \n" +
            "                    \"teacher\": \"dr T.Bodziony\", \n" +
            "                    \"type\": \"W\"\n" +
            "                }\n" +
            "            ], \n" +
            "            [], \n" +
            "            [], \n" +
            "            [\n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"Podstawy informatyki\", \n" +
            "                        \"Ty.N\", \n" +
            "                        \"dr D.Frejlichowski\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"C\", \n" +
            "                        \"WI2- 10\"\n" +
            "                    ], \n" +
            "                    \"name\": \"Podstawy informatyki\", \n" +
            "                    \"rawWG\": \"Ty.N\", \n" +
            "                    \"room\": \"WI2- 10\", \n" +
            "                    \"teacher\": \"dr D.Frejlichowski\", \n" +
            "                    \"type\": \"C\"\n" +
            "                }\n" +
            "            ], \n" +
            "            [\n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"Podstawy informatyki\", \n" +
            "                        \"Ty.N\", \n" +
            "                        \"dr D.Frejlichowski\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"C\", \n" +
            "                        \"WI2- 10\"\n" +
            "                    ], \n" +
            "                    \"name\": \"Podstawy informatyki\", \n" +
            "                    \"rawWG\": \"Ty.N\", \n" +
            "                    \"room\": \"WI2- 10\", \n" +
            "                    \"teacher\": \"dr D.Frejlichowski\", \n" +
            "                    \"type\": \"C\"\n" +
            "                }\n" +
            "            ], \n" +
            "            [], \n" +
            "            [], \n" +
            "            [], \n" +
            "            [], \n" +
            "            [], \n" +
            "            [], \n" +
            "            []\n" +
            "        ], \n" +
            "        [\n" +
            "            [\n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"Podstawy informatyki\", \n" +
            "                        \"dr K.Ma\\u0142ecki\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"W\", \n" +
            "                        \"WI2- 126\"\n" +
            "                    ], \n" +
            "                    \"name\": \"Podstawy informatyki\", \n" +
            "                    \"rawWG\": null, \n" +
            "                    \"room\": \"WI2- 126\", \n" +
            "                    \"teacher\": \"dr K.Ma\\u0142ecki\", \n" +
            "                    \"type\": \"W\"\n" +
            "                }\n" +
            "            ], \n" +
            "            [\n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"Podstawy informatyki\", \n" +
            "                        \"dr K.Ma\\u0142ecki\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"W\", \n" +
            "                        \"WI2- 126\"\n" +
            "                    ], \n" +
            "                    \"name\": \"Podstawy informatyki\", \n" +
            "                    \"rawWG\": null, \n" +
            "                    \"room\": \"WI2- 126\", \n" +
            "                    \"teacher\": \"dr K.Ma\\u0142ecki\", \n" +
            "                    \"type\": \"W\"\n" +
            "                }\n" +
            "            ], \n" +
            "            [\n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"Analiza matematyczna i algebra liniowa I\", \n" +
            "                        \"dr Z.St\\u0119pie\\u0144\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"C\", \n" +
            "                        \"WI2- 17\"\n" +
            "                    ], \n" +
            "                    \"name\": \"Analiza matematyczna i algebra liniowa I\", \n" +
            "                    \"rawWG\": null, \n" +
            "                    \"room\": \"WI2- 17\", \n" +
            "                    \"teacher\": \"dr Z.St\\u0119pie\\u0144\", \n" +
            "                    \"type\": \"C\"\n" +
            "                }\n" +
            "            ], \n" +
            "            [\n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"Analiza matematyczna i algebra liniowa I\", \n" +
            "                        \"dr Z.St\\u0119pie\\u0144\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"C\", \n" +
            "                        \"WI2- 17\"\n" +
            "                    ], \n" +
            "                    \"name\": \"Analiza matematyczna i algebra liniowa I\", \n" +
            "                    \"rawWG\": null, \n" +
            "                    \"room\": \"WI2- 17\", \n" +
            "                    \"teacher\": \"dr Z.St\\u0119pie\\u0144\", \n" +
            "                    \"type\": \"C\"\n" +
            "                }\n" +
            "            ], \n" +
            "            [\n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"Podstawy informatyki\", \n" +
            "                        \"gr.A\", \n" +
            "                        \"dr K.Ma\\u0142ecki\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"L\", \n" +
            "                        \"WI1- 011\"\n" +
            "                    ], \n" +
            "                    \"name\": \"Podstawy informatyki\", \n" +
            "                    \"rawWG\": \"gr.A\", \n" +
            "                    \"room\": \"WI1- 011\", \n" +
            "                    \"teacher\": \"dr K.Ma\\u0142ecki\", \n" +
            "                    \"type\": \"L\"\n" +
            "                }\n" +
            "            ], \n" +
            "            [\n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"Podstawy informatyki\", \n" +
            "                        \"gr.A\", \n" +
            "                        \"dr K.Ma\\u0142ecki\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"L\", \n" +
            "                        \"WI1- 011\"\n" +
            "                    ], \n" +
            "                    \"name\": \"Podstawy informatyki\", \n" +
            "                    \"rawWG\": \"gr.A\", \n" +
            "                    \"room\": \"WI1- 011\", \n" +
            "                    \"teacher\": \"dr K.Ma\\u0142ecki\", \n" +
            "                    \"type\": \"L\"\n" +
            "                }\n" +
            "            ], \n" +
            "            [\n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"Elektronika\", \n" +
            "                        \"dr Z.Rudak\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"W\", \n" +
            "                        \"WI2- 126\"\n" +
            "                    ], \n" +
            "                    \"name\": \"Elektronika\", \n" +
            "                    \"rawWG\": null, \n" +
            "                    \"room\": \"WI2- 126\", \n" +
            "                    \"teacher\": \"dr Z.Rudak\", \n" +
            "                    \"type\": \"W\"\n" +
            "                }\n" +
            "            ], \n" +
            "            [\n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"Elektronika\", \n" +
            "                        \"dr Z.Rudak\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"W\", \n" +
            "                        \"WI2- 126\"\n" +
            "                    ], \n" +
            "                    \"name\": \"Elektronika\", \n" +
            "                    \"rawWG\": null, \n" +
            "                    \"room\": \"WI2- 126\", \n" +
            "                    \"teacher\": \"dr Z.Rudak\", \n" +
            "                    \"type\": \"W\"\n" +
            "                }\n" +
            "            ], \n" +
            "            [], \n" +
            "            [], \n" +
            "            [], \n" +
            "            [], \n" +
            "            []\n" +
            "        ], \n" +
            "        [\n" +
            "            [], \n" +
            "            [], \n" +
            "            [\n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"Podstawy informatyki\", \n" +
            "                        \"gr.B\", \n" +
            "                        \"dr K.Ma\\u0142ecki\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"L\", \n" +
            "                        \"WI1- 011\"\n" +
            "                    ], \n" +
            "                    \"name\": \"Podstawy informatyki\", \n" +
            "                    \"rawWG\": \"gr.B\", \n" +
            "                    \"room\": \"WI1- 011\", \n" +
            "                    \"teacher\": \"dr K.Ma\\u0142ecki\", \n" +
            "                    \"type\": \"L\"\n" +
            "                }\n" +
            "            ], \n" +
            "            [\n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"Podstawy informatyki\", \n" +
            "                        \"gr.B\", \n" +
            "                        \"dr K.Ma\\u0142ecki\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"L\", \n" +
            "                        \"WI1- 011\"\n" +
            "                    ], \n" +
            "                    \"name\": \"Podstawy informatyki\", \n" +
            "                    \"rawWG\": \"gr.B\", \n" +
            "                    \"room\": \"WI1- 011\", \n" +
            "                    \"teacher\": \"dr K.Ma\\u0142ecki\", \n" +
            "                    \"type\": \"L\"\n" +
            "                }\n" +
            "            ], \n" +
            "            [\n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"Elementy cyfrowe i uk\\u0142ady logiczne\", \n" +
            "                        \"dr G.Cariowa\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"W\", \n" +
            "                        \"WI2- 126\"\n" +
            "                    ], \n" +
            "                    \"name\": \"Elementy cyfrowe i uk\\u0142ady logiczne\", \n" +
            "                    \"rawWG\": null, \n" +
            "                    \"room\": \"WI2- 126\", \n" +
            "                    \"teacher\": \"dr G.Cariowa\", \n" +
            "                    \"type\": \"W\"\n" +
            "                }\n" +
            "            ], \n" +
            "            [\n" +
            "                {\n" +
            "                    \"_debugLeftTexts\": [], \n" +
            "                    \"_debugMiddleTexts\": [\n" +
            "                        \"Elementy cyfrowe i uk\\u0142ady logiczne\", \n" +
            "                        \"dr G.Cariowa\"\n" +
            "                    ], \n" +
            "                    \"_debugRightTexts\": [\n" +
            "                        \"W\", \n" +
            "                        \"WI2- 126\"\n" +
            "                    ], \n" +
            "                    \"name\": \"Elementy cyfrowe i uk\\u0142ady logiczne\", \n" +
            "                    \"rawWG\": null, \n" +
            "                    \"room\": \"WI2- 126\", \n" +
            "                    \"teacher\": \"dr G.Cariowa\", \n" +
            "                    \"type\": \"W\"\n" +
            "                }\n" +
            "            ], \n" +
            "            [], \n" +
            "            [], \n" +
            "            [], \n" +
            "            [], \n" +
            "            [], \n" +
            "            [], \n" +
            "            []\n" +
            "        ]\n" +
            "    ]\n" +
            "}";



    private Timetable parseSchedule(JSONObject json) {
        try {
            // Parse hours list
            JSONArray hoursJsonArray = json.getJSONArray("hours");
            int hoursCount = hoursJsonArray.length();
            Timetable.TimeRange[] ranges = new Timetable.TimeRange[hoursCount];
            for (int i = 0; i < hoursCount; i++) {
                JSONArray hourJsonArray = hoursJsonArray.getJSONArray(i);
                ranges[i] = new Timetable.TimeRange(
                        hourJsonArray.getJSONArray(0).getInt(0),
                        hourJsonArray.getJSONArray(0).getInt(1),
                        hourJsonArray.getJSONArray(1).getInt(0),
                        hourJsonArray.getJSONArray(1).getInt(1)
                );
            }

            // Parse days
            JSONArray weekdaySchedulesJson = json.getJSONArray("weekdaySchedules");
            Timetable.Hour[][] weekdaysSchedules = new Timetable.Hour[5][];
            List<Timetable.Hour> daySchedule = new ArrayList<>();
            for (int weekday = 0; weekday < 5; weekday++) {
                JSONArray dayScheduleJson = weekdaySchedulesJson.getJSONArray(weekday);
                daySchedule.clear();

                for (int hour = 0; hour < hoursCount; hour++) {
                    JSONArray tasksInHour = dayScheduleJson.getJSONArray(hour);
                    for (int i = 0; i < tasksInHour.length(); i++) {
                        JSONObject taskJson = tasksInHour.getJSONObject(i);
                        daySchedule.add(new Timetable.Hour(
                                taskJson.getString("name"),
                                taskJson.getString("type"),
                                taskJson.getString("room"),
                                taskJson.getString("teacher"),
                                taskJson.getString("rawWG"),
                                ranges[hour]
                        ));
                    }
                }

                weekdaysSchedules[weekday] = daySchedule.toArray(new Timetable.Hour[daySchedule.size()]);
            }

            return new Timetable(weekdaysSchedules);

        } catch (Exception e) {
            // TODO: handling
            e.printStackTrace();
            return null;
        }
    }

    public void getSchedule(final ScheduleLoadedListener callback) {
        // Just mocking being async
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Timetable timetable = null;
                try {
                    timetable = parseSchedule((JSONObject) new JSONTokener(TEST_SCHEDULE).nextValue());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                callback.onScheduleLoaded(timetable);
            }
        }, 10);
    }

    public interface ScheduleLoadedListener {
        void onScheduleLoaded(Timetable timetable);
    }
}
