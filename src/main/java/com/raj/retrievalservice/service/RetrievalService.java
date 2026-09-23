package com.raj.retrievalservice.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RetrievalService {

    // typed as the INTERFACE now — Qdrant is provided by the config bean
    private final EmbeddingStore<TextSegment> store;

    public RetrievalService(EmbeddingStore<TextSegment> store) {
        this.store = store;
    }

    public void store(String text, List<Float> vector) {
        Embedding embedding = Embedding.from(vector);
        TextSegment segment = TextSegment.from(text);
        store.add(embedding, segment);        // ← UNCHANGED
    }

    public List<String> search(List<Float> queryVector, int topK) {
        Embedding queryEmbedding = Embedding.from(queryVector);

        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(topK)
                .build();

        EmbeddingSearchResult<TextSegment> result = store.search(request);   // ← UNCHANGED

        return result.matches().stream()
                .map(match -> match.embedded().text())
                .toList();
    }
}