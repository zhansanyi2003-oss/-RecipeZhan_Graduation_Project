package org.zhan.recipe_backend.config;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "recipe.storage.type", havingValue = "azure")
public class AzureBlobStorageConfig {

    @Bean
    public BlobServiceClient blobServiceClient(
            @Value("${recipe.azure.storage.account-name}") String accountName) {

        String endpoint = "https://" + accountName + ".blob.core.windows.net";
        return new BlobServiceClientBuilder()
                .endpoint(endpoint)
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();
    }

    @Bean
    public BlobContainerClient blobContainerClient(
            BlobServiceClient blobServiceClient,
            @Value("${recipe.azure.storage.container-name}") String containerName) {

        return blobServiceClient.getBlobContainerClient(containerName);
    }
}
