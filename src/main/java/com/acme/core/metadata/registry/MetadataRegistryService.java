
package com.acme.core.metadata.registry;

import com.acme.core.metadata.model.MetaDefinition;
import java.util.Map;

public interface MetadataRegistryService {
    Map<String, MetaDefinition> getAll();
    void refresh();
}
