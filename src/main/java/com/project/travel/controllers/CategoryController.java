package com.project.travel.controllers;

import com.project.travel.models.Category;
import com.project.travel.payload.request.CategoryRequest;
import com.project.travel.repository.CategoryRepository;
import com.project.travel.services.FilesStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/category")
public class CategoryController {
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    FilesStorageService storageService;

    @Value("${app.fileURL}")
    private String fileURL;

    private static final Logger logger = LoggerFactory.getLogger(PlaceController.class);

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<Category> getTourById(@PathVariable(value = "id") Integer id) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(category);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> createCategory(
            @RequestParam("file") MultipartFile file,
            @Valid @ModelAttribute CategoryRequest categoryRequest
    ) {
        Category category = new Category();
        category.setKey(categoryRequest.getKey());
        category.setName(categoryRequest.getName());

        String filename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        if (!filename.equals("")){
            category.setImage(fileURL + filename);
            try{
                storageService.save(file);
            }catch(Exception e){
                logger.error(e.getMessage());
            }
        }
        categoryRepository.save(category);
        return ResponseEntity.ok(category);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file,
            @Valid @ModelAttribute CategoryRequest categoryRequest
    ) {
        Category category = categoryRepository.findById(id)
                .orElse(null);
        if(category != null) {
            category.setKey(categoryRequest.getKey());
            category.setName(categoryRequest.getName());

            String filename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            if (!filename.equals("")){
                category.setImage(fileURL + filename);
                try{
                    storageService.save(file);
                }catch(Exception e){
                    logger.error(e.getMessage());
                }
            }
            categoryRepository.save(category);
            return ResponseEntity.ok(category);
        }
        return ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable(value = "id") Integer id) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category == null) {
            return ResponseEntity.notFound().build();
        }
        categoryRepository.delete(category);
        return ResponseEntity.noContent().build();
    };
}
