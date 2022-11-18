package mercury.glue.wiring;

import static java.util.Locale.ENGLISH;

import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import cucumber.api.TypeRegistry;
import cucumber.api.TypeRegistryConfigurer;
import io.cucumber.datatable.DataTableType;
import io.cucumber.datatable.TableEntryTransformer;
import mercury.glue.step.ActivityRestartSteps;
import mercury.glue.step.courseware.ThemeSteps;
import mercury.helpers.plugin.PluginHelper;

public class TypeRegistryConfiguration implements TypeRegistryConfigurer {
    @Override
    public Locale locale() {
        return ENGLISH;
    }

    @Override
    public void configureTypeRegistry(TypeRegistry typeRegistry) {

        typeRegistry.defineDataTableType(new DataTableType(
                PluginHelper.CollaboratorItem.class,
                (Map<String, String> row) -> new ObjectMapper().convertValue(row, PluginHelper.CollaboratorItem.class))
        );

        typeRegistry.defineDataTableType(new DataTableType(
                ActivityRestartSteps.ProgressItem.class,
                (TableEntryTransformer<ActivityRestartSteps.ProgressItem>) entry -> new ActivityRestartSteps.ProgressItem(
                        entry.get("id"),
                        (entry.get("attempt").isEmpty() ? null : Integer.valueOf(entry.get("attempt"))),
                        (entry.get("progress").isEmpty() ? null : Double.valueOf(entry.get("progress")))
                )));

        typeRegistry.defineDataTableType(new DataTableType(
                ThemeSteps.IconLibraryInfo.class,
                (Map<String, String> row) -> new ObjectMapper().convertValue(row, ThemeSteps.IconLibraryInfo.class))
        );
    }
}
