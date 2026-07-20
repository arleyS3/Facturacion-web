package com.facturacion.api.web.controller;

import com.facturacion.api.web.dto.CatalogCrudRequest;
import com.facturacion.api.web.dto.CatalogResponse;
import com.facturacion.api.web.models.*;
import com.facturacion.api.web.repositories.*;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/admin/catalogos")
public class AdminCatalogController {

    private static final Set<String> IGV_TYPES = Set.of("tipos-afectacion-igv");

    private final Map<String, JpaRepository<?, Long>> repos;
    private final TipoOperacionRepository tipoOperacionRepo;
    private final TipoNotaCreditoRepository tipoNotaCreditoRepo;
    private final TipoNotaDebitoRepository tipoNotaDebitoRepo;
    private final TipoSistemaIscRepository tipoSistemaIscRepo;
    private final TipoAfectacionIgvRepository tipoAfectacionIgvRepo;
    private final TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepo;
    private final MotivoTrasladoRepository motivoTrasladoRepo;

    public AdminCatalogController(
            TipoOperacionRepository tipoOperacionRepository,
            TipoNotaCreditoRepository tipoNotaCreditoRepository,
            TipoNotaDebitoRepository tipoNotaDebitoRepository,
            TipoSistemaIscRepository tipoSistemaIscRepository,
            TipoAfectacionIgvRepository tipoAfectacionIgvRepository,
            TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository,
            MotivoTrasladoRepository motivoTrasladoRepository) {
        this.tipoOperacionRepo = tipoOperacionRepository;
        this.tipoNotaCreditoRepo = tipoNotaCreditoRepository;
        this.tipoNotaDebitoRepo = tipoNotaDebitoRepository;
        this.tipoSistemaIscRepo = tipoSistemaIscRepository;
        this.tipoAfectacionIgvRepo = tipoAfectacionIgvRepository;
        this.tipoDocumentoIdentidadRepo = tipoDocumentoIdentidadRepository;
        this.motivoTrasladoRepo = motivoTrasladoRepository;
        this.repos = Map.of(
                "tipos-operacion", tipoOperacionRepository,
                "tipos-nota-credito", tipoNotaCreditoRepository,
                "tipos-nota-debito", tipoNotaDebitoRepository,
                "sistemas-isc", tipoSistemaIscRepository,
                "tipos-afectacion-igv", tipoAfectacionIgvRepository,
                "tipos-documento-identidad", tipoDocumentoIdentidadRepository,
                "motivos-traslado", motivoTrasladoRepository
        );
    }

    @PostMapping("/{tipo}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> crear(@PathVariable String tipo, @Valid @RequestBody CatalogCrudRequest request) {
        var repo = resolveRepo(tipo);
        if (repo == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tipo de catalogo invalido: " + tipo));
        }
        try {
            var entity = createEntity(tipo, request);
            JpaRepository repoCast = repo;
            var saved = repoCast.save(entity);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "El codigo ya existe"));
        }
    }

    @PutMapping("/{tipo}/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> actualizar(@PathVariable String tipo, @PathVariable Long id,
                                        @Valid @RequestBody CatalogCrudRequest request) {
        var repo = resolveRepo(tipo);
        if (repo == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tipo de catalogo invalido: " + tipo));
        }
        var opt = repo.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Registro no encontrado"));
        }
        try {
            var entity = opt.get();
            updateEntity(entity, request);
            JpaRepository repoCast = repo;
            var saved = repoCast.save(entity);
            return ResponseEntity.ok(toResponse(saved));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "El codigo ya existe"));
        }
    }

    @GetMapping("/{tipo}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listar(@PathVariable String tipo) {
        var repo = resolveRepo(tipo);
        if (repo == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tipo de catalogo invalido: " + tipo));
        }
        var all = findAllIncludingInactive(tipo);
        return ResponseEntity.ok(all.stream().map(this::toResponse).toList());
    }

    @PatchMapping("/{tipo}/{id}/reactivar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> reactivar(@PathVariable String tipo, @PathVariable Long id) {
        var repo = resolveRepo(tipo);
        if (repo == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tipo de catalogo invalido: " + tipo));
        }
        var entity = findByIdIncludingInactive(tipo, id);
        if (entity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Registro no encontrado"));
        }
        setActivo(entity, true);
        JpaRepository repoCast = repo;
        repoCast.save(entity);
        return ResponseEntity.ok(toResponse(entity));
    }

    @DeleteMapping("/{tipo}/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> desactivar(@PathVariable String tipo, @PathVariable Long id) {
        var repo = resolveRepo(tipo);
        if (repo == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tipo de catalogo invalido: " + tipo));
        }
        var opt = repo.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Registro no encontrado"));
        }
        var entity = opt.get();
        if (entity instanceof TipoOperacionEntity e) { e.setActivo(false); e.setActualizadoAt(LocalDateTime.now()); }
        else if (entity instanceof TipoNotaCreditoEntity e) { e.setActivo(false); e.setActualizadoAt(LocalDateTime.now()); }
        else if (entity instanceof TipoNotaDebitoEntity e) { e.setActivo(false); e.setActualizadoAt(LocalDateTime.now()); }
        else if (entity instanceof TipoSistemaIscEntity e) { e.setActivo(false); e.setActualizadoAt(LocalDateTime.now()); }
        else if (entity instanceof TipoAfectacionIgvEntity e) { e.setActivo(false); e.setActualizadoAt(LocalDateTime.now()); }
        else if (entity instanceof TipoDocumentoIdentidadEntity e) { e.setActivo(false); e.setActualizadoAt(LocalDateTime.now()); }
        else if (entity instanceof MotivoTrasladoEntity e) { e.setActivo(false); e.setActualizadoAt(LocalDateTime.now()); }
        JpaRepository repoCast = repo;
        repoCast.save(entity);
        return ResponseEntity.noContent().build();
    }

    // -- helpers --

    private JpaRepository<?, Long> resolveRepo(String tipo) {
        return repos.get(tipo);
    }

    private Object createEntity(String tipo, CatalogCrudRequest request) {
        Object entity;
        switch (tipo) {
            case "tipos-operacion" -> {
                var e = new TipoOperacionEntity();
                e.setCodigo(request.getCodigo());
                e.setDescripcion(request.getDescripcion());
                e.setActivo(true);
                e.setCreadoAt(LocalDateTime.now());
                e.setActualizadoAt(LocalDateTime.now());
                entity = e;
            }
            case "tipos-nota-credito" -> {
                var e = new TipoNotaCreditoEntity();
                e.setCodigo(request.getCodigo());
                e.setDescripcion(request.getDescripcion());
                e.setActivo(true);
                e.setCreadoAt(LocalDateTime.now());
                e.setActualizadoAt(LocalDateTime.now());
                entity = e;
            }
            case "tipos-nota-debito" -> {
                var e = new TipoNotaDebitoEntity();
                e.setCodigo(request.getCodigo());
                e.setDescripcion(request.getDescripcion());
                e.setActivo(true);
                e.setCreadoAt(LocalDateTime.now());
                e.setActualizadoAt(LocalDateTime.now());
                entity = e;
            }
            case "sistemas-isc" -> {
                var e = new TipoSistemaIscEntity();
                e.setCodigo(request.getCodigo());
                e.setDescripcion(request.getDescripcion());
                e.setActivo(true);
                e.setCreadoAt(LocalDateTime.now());
                e.setActualizadoAt(LocalDateTime.now());
                entity = e;
            }
            case "tipos-afectacion-igv" -> {
                var e = new TipoAfectacionIgvEntity();
                e.setCodigo(request.getCodigo());
                e.setDescripcion(request.getDescripcion());
                e.setCodigoTributario(request.getCodigoTributario());
                e.setActivo(true);
                e.setCreadoAt(LocalDateTime.now());
                e.setActualizadoAt(LocalDateTime.now());
                entity = e;
            }
            case "tipos-documento-identidad" -> {
                var e = new TipoDocumentoIdentidadEntity();
                e.setCodigo(request.getCodigo());
                e.setDescripcion(request.getDescripcion());
                e.setActivo(true);
                e.setCreadoAt(LocalDateTime.now());
                e.setActualizadoAt(LocalDateTime.now());
                entity = e;
            }
            case "motivos-traslado" -> {
                var e = new MotivoTrasladoEntity();
                e.setCodigo(request.getCodigo());
                e.setDescripcion(request.getDescripcion());
                e.setActivo(true);
                e.setCreadoAt(LocalDateTime.now());
                e.setActualizadoAt(LocalDateTime.now());
                entity = e;
            }
            default -> throw new IllegalArgumentException("Tipo de catalogo invalido: " + tipo);
        }
        return entity;
    }

    private void updateEntity(Object entity, CatalogCrudRequest request) {
        if (request.getCodigo() != null && !request.getCodigo().isBlank()) {
            if (entity instanceof TipoOperacionEntity e) e.setCodigo(request.getCodigo());
            else if (entity instanceof TipoNotaCreditoEntity e) e.setCodigo(request.getCodigo());
            else if (entity instanceof TipoNotaDebitoEntity e) e.setCodigo(request.getCodigo());
            else if (entity instanceof TipoSistemaIscEntity e) e.setCodigo(request.getCodigo());
            else if (entity instanceof TipoAfectacionIgvEntity e) e.setCodigo(request.getCodigo());
            else if (entity instanceof TipoDocumentoIdentidadEntity e) e.setCodigo(request.getCodigo());
            else if (entity instanceof MotivoTrasladoEntity e) e.setCodigo(request.getCodigo());
        }
        if (entity instanceof TipoOperacionEntity e) {
            e.setDescripcion(request.getDescripcion());
            e.setActualizadoAt(LocalDateTime.now());
        } else if (entity instanceof TipoNotaCreditoEntity e) {
            e.setDescripcion(request.getDescripcion());
            e.setActualizadoAt(LocalDateTime.now());
        } else if (entity instanceof TipoNotaDebitoEntity e) {
            e.setDescripcion(request.getDescripcion());
            e.setActualizadoAt(LocalDateTime.now());
        } else if (entity instanceof TipoSistemaIscEntity e) {
            e.setDescripcion(request.getDescripcion());
            e.setActualizadoAt(LocalDateTime.now());
        } else if (entity instanceof TipoAfectacionIgvEntity e) {
            e.setDescripcion(request.getDescripcion());
            e.setCodigoTributario(request.getCodigoTributario());
            e.setActualizadoAt(LocalDateTime.now());
        } else if (entity instanceof TipoDocumentoIdentidadEntity e) {
            e.setDescripcion(request.getDescripcion());
            e.setActualizadoAt(LocalDateTime.now());
        } else if (entity instanceof MotivoTrasladoEntity e) {
            e.setDescripcion(request.getDescripcion());
            e.setActualizadoAt(LocalDateTime.now());
        }
    }

    private CatalogResponse toResponse(Object entity) {
        if (entity instanceof TipoAfectacionIgvEntity e) {
            return new CatalogResponse(e.getId(), e.getCodigo(), e.getDescripcion(), e.getActivo(), e.getCodigoTributario());
        }
        return switch (entity) {
            case TipoOperacionEntity e -> new CatalogResponse(e.getId(), e.getCodigo(), e.getDescripcion(), e.getActivo(), null);
            case TipoNotaCreditoEntity e -> new CatalogResponse(e.getId(), e.getCodigo(), e.getDescripcion(), e.getActivo(), null);
            case TipoNotaDebitoEntity e -> new CatalogResponse(e.getId(), e.getCodigo(), e.getDescripcion(), e.getActivo(), null);
            case TipoSistemaIscEntity e -> new CatalogResponse(e.getId(), e.getCodigo(), e.getDescripcion(), e.getActivo(), null);
            case TipoDocumentoIdentidadEntity e -> new CatalogResponse(e.getId(), e.getCodigo(), e.getDescripcion(), e.getActivo(), null);
            case MotivoTrasladoEntity e -> new CatalogResponse(e.getId(), e.getCodigo(), e.getDescripcion(), e.getActivo(), null);
            default -> throw new IllegalArgumentException("Tipo de entidad desconocido");
        };
    }

    private List<?> findAllIncludingInactive(String tipo) {
        return switch (tipo) {
            case "tipos-operacion" -> tipoOperacionRepo.findAllIncludingInactive();
            case "tipos-nota-credito" -> tipoNotaCreditoRepo.findAllIncludingInactive();
            case "tipos-nota-debito" -> tipoNotaDebitoRepo.findAllIncludingInactive();
            case "sistemas-isc" -> tipoSistemaIscRepo.findAllIncludingInactive();
            case "tipos-afectacion-igv" -> tipoAfectacionIgvRepo.findAllIncludingInactive();
            case "tipos-documento-identidad" -> tipoDocumentoIdentidadRepo.findAllIncludingInactive();
            case "motivos-traslado" -> motivoTrasladoRepo.findAllIncludingInactive();
            default -> throw new IllegalArgumentException("Tipo de catalogo invalido: " + tipo);
        };
    }

    private Object findByIdIncludingInactive(String tipo, Long id) {
        return switch (tipo) {
            case "tipos-operacion" -> tipoOperacionRepo.findByIdIncludingInactive(id);
            case "tipos-nota-credito" -> tipoNotaCreditoRepo.findByIdIncludingInactive(id);
            case "tipos-nota-debito" -> tipoNotaDebitoRepo.findByIdIncludingInactive(id);
            case "sistemas-isc" -> tipoSistemaIscRepo.findByIdIncludingInactive(id);
            case "tipos-afectacion-igv" -> tipoAfectacionIgvRepo.findByIdIncludingInactive(id);
            case "tipos-documento-identidad" -> tipoDocumentoIdentidadRepo.findByIdIncludingInactive(id);
            case "motivos-traslado" -> motivoTrasladoRepo.findByIdIncludingInactive(id);
            default -> throw new IllegalArgumentException("Tipo de catalogo invalido: " + tipo);
        };
    }

    private void setActivo(Object entity, boolean activo) {
        if (entity instanceof TipoOperacionEntity e) { e.setActivo(activo); e.setActualizadoAt(LocalDateTime.now()); }
        else if (entity instanceof TipoNotaCreditoEntity e) { e.setActivo(activo); e.setActualizadoAt(LocalDateTime.now()); }
        else if (entity instanceof TipoNotaDebitoEntity e) { e.setActivo(activo); e.setActualizadoAt(LocalDateTime.now()); }
        else if (entity instanceof TipoSistemaIscEntity e) { e.setActivo(activo); e.setActualizadoAt(LocalDateTime.now()); }
        else if (entity instanceof TipoAfectacionIgvEntity e) { e.setActivo(activo); e.setActualizadoAt(LocalDateTime.now()); }
        else if (entity instanceof TipoDocumentoIdentidadEntity e) { e.setActivo(activo); e.setActualizadoAt(LocalDateTime.now()); }
        else if (entity instanceof MotivoTrasladoEntity e) { e.setActivo(activo); e.setActualizadoAt(LocalDateTime.now()); }
    }

}
