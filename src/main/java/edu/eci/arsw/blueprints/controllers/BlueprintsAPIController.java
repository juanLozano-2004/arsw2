package edu.eci.arsw.blueprints.controllers;
import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/blueprints") // Cambiado el path base para que coincida con el enunciado y evitar conflictos con el controlador web
public class BlueprintsAPIController {

    private final BlueprintsServices services;

    public BlueprintsAPIController(BlueprintsServices services) {
        this.services = services;
    }

    // GET /api/v1/blueprints
    @Operation
    @GetMapping
    public ResponseEntity<ApiResponse<Set<Blueprint>>> getAll() {
        Set<Blueprint> blueprints = services.getAllBlueprints();
        ApiResponse<Set<Blueprint>> response = new ApiResponse<>(200, "Blueprints retrieved successfully", blueprints);
        return ResponseEntity.ok(response); // 200 OK
    }

    // GET /api/v1/blueprints/{author}
    @Operation
    @GetMapping("/{author}")
    public ResponseEntity<ApiResponse<?>> byAuthor(@PathVariable String author) {
        try {
            Set<Blueprint> blueprints = services.getBlueprintsByAuthor(author);
            ApiResponse<Set<Blueprint>> response = new ApiResponse<>(200, "Blueprints by author retrieved successfully", blueprints);
            return ResponseEntity.ok(response); // 200 OK
        } catch (BlueprintNotFoundException e) {
            ApiResponse<Void> response = new ApiResponse<>(404, e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // 404 Not Found
        }
    }

    // GET /api/v1/blueprints/{author}/{bpname}
    @Operation
    @GetMapping("/{author}/{bpname}")
    public ResponseEntity<ApiResponse<?>> byAuthorAndName(@PathVariable String author, @PathVariable String bpname) {
        try {
            Blueprint blueprint = services.getBlueprint(author, bpname);
            ApiResponse<Blueprint> response = new ApiResponse<>(200, "Blueprint retrieved successfully", blueprint);
            return ResponseEntity.ok(response); // 200 OK
        } catch (BlueprintNotFoundException e) {
            ApiResponse<Void> response = new ApiResponse<>(404, e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // 404 Not Found
        }
    }

    // POST /api/v1/blueprints
    @Operation
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> add(@Valid @RequestBody NewBlueprintRequest req) {
        try {
            Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
            services.addNewBlueprint(bp);
            ApiResponse<Void> response = new ApiResponse<>(201, "Blueprint created successfully", null);
            return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201 Created
        } catch (BlueprintPersistenceException e) {
            ApiResponse<Void> response = new ApiResponse<>(403, e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response); // 403 Forbidden
        }
    }

    // PUT /api/v1/blueprints/{author}/{bpname}/points
    @Operation
    @PutMapping("/{author}/{bpname}/points")
    public ResponseEntity<ApiResponse<Void>> addPoint(@PathVariable String author, @PathVariable String bpname,
                                                      @RequestBody Point p) {
        try {
            services.addPoint(author, bpname, p.x(), p.y());
            ApiResponse<Void> response = new ApiResponse<>(202, "Point added successfully", null);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response); // 202 Accepted
        } catch (BlueprintNotFoundException e) {
            ApiResponse<Void> response = new ApiResponse<>(404, e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // 404 Not Found
        }
    }

    public record NewBlueprintRequest(
            @NotBlank String author,
            @NotBlank String name,
            @Valid List<Point> points
    ) { }

    public record ApiResponse<T>(int code, String message, T data) { }
}