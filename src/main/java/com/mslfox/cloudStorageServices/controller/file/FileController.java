package com.mslfox.cloudStorageServices.controller.file;

import com.mslfox.cloudStorageServices.constant.HeaderNameHolder;
import com.mslfox.cloudStorageServices.dto.file.FileRenameRequest;
import com.mslfox.cloudStorageServices.dto.file.FileRequest;
import com.mslfox.cloudStorageServices.dto.file.FileUploadRequest;
import com.mslfox.cloudStorageServices.model.error.ErrorResponse;
import com.mslfox.cloudStorageServices.model.file.FileInfoResponse;
import com.mslfox.cloudStorageServices.openApi.AuthTokenParameter;
import com.mslfox.cloudStorageServices.service.file.impl.FileServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import netscape.javascript.JSObject;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.*;

import static com.mslfox.cloudStorageServices.util.Base64Util.generateRandomLinkKey;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@ApiResponses({
        @ApiResponse(responseCode = "400", description = "Invalid request format or missing parameters or input data.",
                content = @Content(mediaType = APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Authorization error",
                content = @Content(mediaType = APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
                content = @Content(mediaType = APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(content = @Content(mediaType = TEXT_PLAIN_VALUE,
                schema = @Schema(implementation = String.class)))})
@Tag(name = "Files manager")
@RestController
@RequiredArgsConstructor
@Validated
public class FileController {
    private final FileServiceImpl fileServiceImpl;
    private final Map<String, Resource> oneTimeLinks = new HashMap<>();

    @Operation(description = "Returns list of uploaded files up to specified limit")
    @ApiResponse(responseCode = "200", description = "List of file-info",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = FileInfoResponse.class)))
    @AuthTokenParameter
    @GetMapping("list")
    public List<FileInfoResponse> limitListUploaded(
            @Parameter(description = "Size of returned list uploaded files", required = true)
            @RequestParam("limit") @Min(1) @Valid int limit) throws RuntimeException {
        return fileServiceImpl.getFileInfoList(limit);
    }

    @Operation(description = "Uploads a file")
    @ApiResponse(responseCode = "200", description = "Upload operation success message",
            content = @Content(mediaType = TEXT_PLAIN_VALUE,
                    schema = @Schema(implementation = String.class)))
    @AuthTokenParameter
    @PostMapping("file")
    public String handleFileUpload(
            @ModelAttribute @Valid FileUploadRequest fileUploadRequest) throws RuntimeException {
        return fileServiceImpl.upload(fileUploadRequest);
    }
    @Operation(description = "Deletes a file")
    @ApiResponse(responseCode = "200", description = "'Delete operation' success message",
            content = @Content(mediaType = TEXT_PLAIN_VALUE,
                    schema = @Schema(implementation = String.class)))
    @AuthTokenParameter
    @DeleteMapping("/file")
    public String delete(
            @Parameter(description = "Filename to delete", required = true)
            @RequestParam @Valid @NotEmpty @NotBlank String filename) {
        final var fileRequest = new FileRequest(filename);
        return fileServiceImpl.deleteFile(fileRequest);
    }
    @Operation(description = "Generate one-time download link for file")
    @ApiResponse(responseCode = "200", description = "One-Time Download Link for File")
    @AuthTokenParameter
    @GetMapping("/file")
    public ResponseEntity<String> getFileLinkKey(
            @Parameter(description = "Name of the file to generate one-time download link for")
            @RequestParam @Valid @NotEmpty @NotBlank String filename ) {
        final var fileRequest = new FileRequest(filename);
        final var fileResource = fileServiceImpl.getFileResource(fileRequest);
        String linkKey = generateRandomLinkKey();
        oneTimeLinks.put(linkKey, fileResource);
        return ResponseEntity.ok().body(linkKey);
    }
    @Operation(description = "Serve a file using a one-time download link")
    @ApiResponse(responseCode = "200", description = "Download operation success message")
    @GetMapping("/download/{linkKey}")
    public ResponseEntity<Resource> downloadByLinkKey(
            @Parameter(description = "Unique key to access the one-time download link")
            @PathVariable String linkKey) {
        final var resource = oneTimeLinks.remove(linkKey);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", resource.getFilename());
        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }

    @Operation(description = "Renames file")
    @ApiResponse(responseCode = "200", description = "Rename operation success message",
            content = @Content(mediaType = TEXT_PLAIN_VALUE,
                    schema = @Schema(implementation = String.class)))
    @AuthTokenParameter
    @PutMapping("/file")
    public String handleFileRename(
            @Parameter(description = "Filename to rename", required = true)
            @RequestParam @Valid @NotEmpty @NotBlank String filename,
            @Parameter(description = "New filename", required = true)
            @RequestBody @Valid @NotNull FileRequest fileRequest
    ) {
        final var fileRenameRequest = new FileRenameRequest();
        fileRenameRequest.setNewFilename(fileRequest.getFilename());
        fileRenameRequest.setToUpdateFilename(filename);
        return fileServiceImpl.renameFile(fileRenameRequest);
    }
}

