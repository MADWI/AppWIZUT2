package pl.edu.zut.mad.appwizut2.utils;

import android.content.Context;

public class Constants {
    /**
     * Directory name inside {@link Context#getFilesDir()} from which files are provided with FileProvider
     */
    public static final String FILE_PROVIDER_FILE_PATH = "provided";

    /**
     * Authority of out FileProvider
     * @see #FILE_PROVIDER_FILE_PATH
     */
    public static final String FILE_PROVIDER_AUTHORITY = "pl.edu.zut.mad.appwizut2.files";

    /**
     * Path where parsed schedules are downloaded from
     */
    public static final String PARSED_SCHEDULE_URL = "http://bm29640.zut.edu.pl/parsed-schedules/PlanGrup/%s.json";

    /**
     * Path where pdf schedules are downloaded from
     */
    public static final String PDF_SCHEDULE_URL = "http://wi.zut.edu.pl/plan/Wydruki/PlanGrup/%s.pdf";
}
