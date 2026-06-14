package org.sterl.llmpeon;

import org.sterl.llmpeon.ai.ConfiguredChatModel;
import org.sterl.llmpeon.prompt.PromptLoader;
import org.sterl.llmpeon.tool.ToolService;

public class AiDeveloperService extends AbstractChatService {

    private static final String BASE_PROMPT = PromptLoader.loadWithDefault("developer.txt");

    public AiDeveloperService(ConfiguredChatModel configuredModel,
            ToolService toolService) {
        super(configuredModel, toolService);
    }

    @Override
    protected String getSystemPrompt() {
        return BASE_PROMPT;
    }

    protected double getTemperature() {
        return configuredModel.getConfig().getDevTemperature();
    }

    @Override
    protected String resolveAgentModel() {
        return configuredModel.getConfig().getModel();
    }
}
