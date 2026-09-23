package com.raj.retrievalservice.controller.RetrievalController;


import com.raj.retrievalservice.service.RetrievalService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/retrieval")
@CrossOrigin(origins = "*")
public class RetrievalController {

    private final RetrievalService retrievalService;

    public RetrievalController(RetrievalService retrievalService) {
        this.retrievalService = retrievalService;
    }

    // store many chunks at once: {"chunks": [{"text": "...", "vector": [...]}, ...]}
    @PostMapping("/store")
    public Map<String, Object> store(@RequestBody StoreRequest request) {
        for (ChunkVector cv : request.chunks()) {
            retrievalService.store(cv.text(), cv.vector());
        }
        return Map.of("stored", request.chunks().size());
    }

    // search: {"queryVector": [...], "topK": 3}
    @PostMapping("/search")
    public Map<String, Object> search(@RequestBody SearchRequest request) {
        List<String> results = retrievalService.search(request.queryVector(), request.topK());
        return Map.of("results", results);
    }

    // request body shapes — records map JSON fields straight onto typed objects
    public record ChunkVector(String text, List<Float> vector) {}
    public record StoreRequest(List<ChunkVector> chunks) {}
    public record SearchRequest(List<Float> queryVector, int topK) {}
}