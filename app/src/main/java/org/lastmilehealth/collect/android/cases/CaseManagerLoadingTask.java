package org.lastmilehealth.collect.android.cases;

import java.util.List;

/**
 * Created by Anton Donchev on 09.05.2017.
 */

public interface CaseManagerLoadingTask extends LoadingTask {

    List<CaseType> getLoadedCases();

}
