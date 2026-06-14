package org.sterl.llmpeon.tool.tools;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sterl.llmpeon.StreamMock;
import org.sterl.llmpeon.ai.ConfiguredChatModel;
import org.sterl.llmpeon.ai.LlmConfig;
import org.sterl.llmpeon.memory.ThreadSafeMemory;
import org.sterl.llmpeon.tool.ToolLoopRequest;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.response.ChatResponse;

class CompactSessionToolTest {

    private StreamMock streamMock;

    @BeforeEach
    void beforeEach() {
        streamMock = new StreamMock();
    }
    
    @Test
    void testCompactSessionUsesConfiguredCompactModel() {
        // GIVEN — config with compactModel="compact-specific-model"
        var config = LlmConfig.builder()
                .model("default-model")
                .compactModel("compact-specific-model")
                .build();
        
        var cm = streamMock.buildMock(r -> ChatResponse.builder()
                .aiMessage(AiMessage.aiMessage("WHAT: Test context summary"))
                .build());
        var configuredModel = new ConfiguredChatModel(config, cm);
        
        var memory = new ThreadSafeMemory();
        memory.add(UserMessage.from("First message"));
        memory.add(AiMessage.from("AI response 1"));
        memory.add(UserMessage.from("Second message"));
        memory.add(AiMessage.from("AI response 2"));
        
        var toolRequest = ToolLoopRequest.builder()
                .chatModel(configuredModel)
                .memory(memory)
                .build();
        
        var subject = new CompactSessionTool();
        subject.withToolRequest(toolRequest);

        // WHEN
        subject.compactSession(null);

        // THEN — ChatRequest should have modelName="compact-specific-model"
        assertThat(streamMock.getLastRequest()).isNotNull();
        assertThat(streamMock.getLastRequest().modelName()).isEqualTo("compact-specific-model");
        
        // AND — memory should be replaced with compacted session message
        var compactedMemory = memory.getCopy();
        assertThat(compactedMemory).hasSize(1);
        assertThat(((UserMessage)compactedMemory.get(0)).singleText())
                .isEqualTo("Session compacted. Resume the task using the preserved context.");
    }
    
    @Test
    void testCompactSessionWithoutCompactModelUsesDefault() {
        // GIVEN — config without compactModel (null)
        var config = LlmConfig.builder()
                .model("default-model")
                .build();
        
        var cm = streamMock.buildMock(r -> ChatResponse.builder()
                .aiMessage(AiMessage.aiMessage("WHAT: Test context summary"))
                .build());
        var configuredModel = new ConfiguredChatModel(config, cm);
        
        var memory = new ThreadSafeMemory();
        memory.add(UserMessage.from("Test message"));
        memory.add(AiMessage.from("AI response"));
        
        var toolRequest = ToolLoopRequest.builder()
                .chatModel(configuredModel)
                .memory(memory)
                .build();
        
        var subject = new CompactSessionTool();
        subject.withToolRequest(toolRequest);

        // WHEN
        subject.compactSession(null);

        // THEN — ChatRequest should have no modelName override (null means provider default)
        assertThat(streamMock.getLastRequest()).isNotNull();
        assertThat(streamMock.getLastRequest().modelName()).isNull();
        
        // AND — memory should be replaced with compacted session message
        var compactedMemory = memory.getCopy();
        assertThat(compactedMemory).hasSize(1);
        assertThat(((UserMessage)compactedMemory.get(0)).singleText())
                .isEqualTo("Session compacted. Resume the task using the preserved context.");
    }
}
