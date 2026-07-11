package com.career.platform.collect.controller;

import com.career.platform.collect.entity.CollectTask;
import com.career.platform.collect.service.CollectTaskService;
import com.career.platform.common.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/collect/task")
public class CollectTaskController {

    private final CollectTaskService service;

    public CollectTaskController(CollectTaskService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('collect:view')")
    public ApiResponse<List<CollectTask>> list() {
        return ApiResponse.success(service.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('collect:view')")
    public ApiResponse<CollectTask> getById(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @GetMapping("/by-source/{sourceId}")
    @PreAuthorize("hasAuthority('collect:view')")
    public ApiResponse<List<CollectTask>> listBySourceId(@PathVariable Long sourceId) {
        return ApiResponse.success(service.listBySourceId(sourceId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('collect:toggle')")
    public ApiResponse<CollectTask> create(@RequestBody CollectTask task) {
        return ApiResponse.success(service.create(task));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('collect:toggle')")
    public ApiResponse<CollectTask> update(@PathVariable Long id, @RequestBody CollectTask task) {
        return ApiResponse.success(service.update(id, task));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('collect:toggle')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.success(null);
    }
}
