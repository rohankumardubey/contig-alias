/*
 * Copyright 2020 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ebi.eva.contigalias.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import uk.ac.ebi.eva.contigalias.entities.AssemblyEntity;
import uk.ac.ebi.eva.contigalias.entities.ChromosomeEntity;
import uk.ac.ebi.eva.contigalias.entitygenerator.AssemblyGenerator;
import uk.ac.ebi.eva.contigalias.entitygenerator.ChromosomeGenerator;
import uk.ac.ebi.eva.contigalias.service.AliasService;
import uk.ac.ebi.eva.contigalias.service.AssemblyService;
import uk.ac.ebi.eva.contigalias.service.ChromosomeService;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.DEFAULT_PAGE_NUMBER;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.DEFAULT_PAGE_REQUEST;
import static uk.ac.ebi.eva.contigalias.controller.BaseController.DEFAULT_PAGE_SIZE;

public class ContigAliasControllerTest {

    private ContigAliasController controller;

    @Nested
    class AssemblyServiceTests {

        private final AssemblyEntity entity = AssemblyGenerator.generate();

        @BeforeEach
        void setUp() {
            AssemblyService mockAssemblyService = mock(AssemblyService.class);
            PageImpl<AssemblyEntity> page = new PageImpl<>(Collections.singletonList(this.entity));
            Mockito.when(mockAssemblyService
                                 .getAssemblyByAccession(this.entity.getGenbank(), DEFAULT_PAGE_REQUEST))
                   .thenReturn(page);
            Mockito.when(mockAssemblyService
                                 .getAssemblyByAccession(this.entity.getRefseq(), DEFAULT_PAGE_REQUEST))
                   .thenReturn(page);
            Mockito.when(mockAssemblyService.getAssemblyByGenbank(this.entity.getGenbank(), DEFAULT_PAGE_REQUEST))
                   .thenReturn(page);
            Mockito.when(mockAssemblyService.getAssemblyByRefseq(this.entity.getRefseq(), DEFAULT_PAGE_REQUEST))
                   .thenReturn(page);
            Mockito.when(mockAssemblyService.getAssemblyByRefseq(this.entity.getRefseq(), DEFAULT_PAGE_REQUEST))
                   .thenReturn(page);

            PagedResourcesAssembler<AssemblyEntity> assembler = mock(PagedResourcesAssembler.class);
            PagedModel<EntityModel<AssemblyEntity>> pagedModel = new PagedModel<>(
                    Collections.singletonList(new EntityModel<>(entity)), null);
            Mockito.when(assembler.toModel(any()))
                   .thenReturn(pagedModel);
            controller = new ContigAliasController(mockAssemblyService, null,null, assembler, null);
        }

        @Test
        public void getAssemblyByAccession() {
            testAssemblyEntityPagedResponse(
                    controller.getAssemblyByAccession(entity.getGenbank(), DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE));
            testAssemblyEntityPagedResponse(
                    controller.getAssemblyByAccession(entity.getRefseq(), DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE));
        }

        @Test
        public void getAssemblyByGenbank() {
            testAssemblyEntityPagedResponse(
                    controller.getAssemblyByGenbank(entity.getGenbank(), DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE));
        }

        @Test
        public void getAssemblyByRefseq() {
            testAssemblyEntityPagedResponse(
                    controller.getAssemblyByRefseq(entity.getRefseq(), DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE));
        }

        void testAssemblyEntityPagedResponse(ResponseEntity<PagedModel<EntityModel<AssemblyEntity>>> response) {
            assertEquals(response.getStatusCode(), HttpStatus.OK);
            assertTrue(response.hasBody());
            PagedModel<EntityModel<AssemblyEntity>> body = response.getBody();
            assertNotNull(body);
            Collection<EntityModel<AssemblyEntity>> content = body.getContent();
            content.forEach(it -> assertAssemblyIdenticalToEntity(it.getContent()));
        }

        void assertAssemblyIdenticalToEntity(AssemblyEntity assembly) {
            assertNotNull(assembly);
            assertEquals(entity.getName(), assembly.getName());
            assertEquals(entity.getOrganism(), assembly.getOrganism());
            assertEquals(entity.getGenbank(), assembly.getGenbank());
            assertEquals(entity.getRefseq(), assembly.getRefseq());
            assertEquals(entity.getTaxid(), assembly.getTaxid());
            assertEquals(entity.isGenbankRefseqIdentical(), assembly.isGenbankRefseqIdentical());
        }

    }

    @Nested
    class AssemblyServiceGetByTaxidTest {

        private final int MAX_CONSECUTIVE_ENTITIES = 5;

        private final long TAX_ID = 342043L;

        private final List<AssemblyEntity> entities = new LinkedList<>();

        @BeforeEach
        void setup() {
            for (int i = 0; i < MAX_CONSECUTIVE_ENTITIES; i++) {
                AssemblyEntity assemblyEntity = AssemblyGenerator.generate(i).setTaxid(TAX_ID);
                entities.add(assemblyEntity);
            }
            AssemblyService mockAssemblyService = mock(AssemblyService.class);

            Mockito.when(mockAssemblyService
                                 .getAssembliesByTaxid(TAX_ID, DEFAULT_PAGE_REQUEST))
                   .thenReturn(new PageImpl<>(entities));

            PagedResourcesAssembler<AssemblyEntity> assembler = mock(PagedResourcesAssembler.class);
            PagedModel<EntityModel<AssemblyEntity>> pagedModel = PagedModel.wrap(entities, null);
            Mockito.when(assembler.toModel(any()))
                   .thenReturn(pagedModel);
            controller = new ContigAliasController(mockAssemblyService, null,null, assembler, null);
        }

        @Test
        void getAssembliesByTaxid() {

            ResponseEntity<PagedModel<EntityModel<AssemblyEntity>>> response = controller.getAssembliesByTaxid(
                    TAX_ID, DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE);
            assertEquals(response.getStatusCode(), HttpStatus.OK);
            assertTrue(response.hasBody());
            PagedModel<EntityModel<AssemblyEntity>> body = response.getBody();
            assertNotNull(body);
            List<EntityModel<AssemblyEntity>> entityList = new LinkedList<>(body.getContent());
            assertNotNull(entityList);
            assertEquals(MAX_CONSECUTIVE_ENTITIES, entityList.size());

            for (int i = 0; i < MAX_CONSECUTIVE_ENTITIES; i++) {
                AssemblyEntity assembly = entityList.get(i).getContent();
                assertNotNull(assembly);
                AssemblyEntity entity = entities.get(i);
                assertEquals(entity.getName(), assembly.getName());
                assertEquals(entity.getOrganism(), assembly.getOrganism());
                assertEquals(entity.getGenbank(), assembly.getGenbank());
                assertEquals(entity.getRefseq(), assembly.getRefseq());
                assertEquals(TAX_ID, assembly.getTaxid());
                assertEquals(entity.isGenbankRefseqIdentical(), assembly.isGenbankRefseqIdentical());
            }
        }

    }

    @Nested
    class ChromosomeServiceTests {

        ChromosomeEntity entity = ChromosomeGenerator.generate();

        @BeforeEach
        void setUp() {
            ChromosomeService mockChromosomeService = mock(ChromosomeService.class);

            Page<ChromosomeEntity> entityListAsPage = new PageImpl<>(
                    Collections.singletonList(entity));
            Mockito.when(mockChromosomeService.getChromosomeByGenbank(entity.getGenbank(), DEFAULT_PAGE_REQUEST))
                   .thenReturn(entityListAsPage);
            Mockito.when(mockChromosomeService.getChromosomeByRefseq(entity.getRefseq(), DEFAULT_PAGE_REQUEST))
                   .thenReturn(entityListAsPage);

            PagedResourcesAssembler<ChromosomeEntity> assembler = mock(PagedResourcesAssembler.class);
            PagedModel<EntityModel<ChromosomeEntity>> chromosomePagedModel = new PagedModel<>(
                    Collections.singletonList(new EntityModel<>(new ChromosomeEntity())), null);
            Mockito.when(assembler.toModel(any()))
                   .thenReturn(chromosomePagedModel);

            controller = new ContigAliasController(null, mockChromosomeService, null,null, assembler);
        }

        @Test
        public void getChromosomeByGenbank() {
            testChromosomeEntityResponse(
                    controller.getChromosomeByGenbank(entity.getGenbank(), DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE));
        }

        @Test
        public void getChromosomeByRefseq() {
            testChromosomeEntityResponse(
                    controller.getChromosomeByRefseq(entity.getRefseq(), DEFAULT_PAGE_NUMBER, DEFAULT_PAGE_SIZE));
        }

        void testChromosomeEntityResponse(ResponseEntity<PagedModel<EntityModel<ChromosomeEntity>>> response) {
            assertEquals(response.getStatusCode(), HttpStatus.OK);
            assertTrue(response.hasBody());
            PagedModel<EntityModel<ChromosomeEntity>> body = response.getBody();
            assertNotNull(body);
            Collection<EntityModel<ChromosomeEntity>> content = body.getContent();
            assertTrue(content.size() > 0);
            content.forEach(it -> assertChromosomeIdenticalToEntity(it.getContent()));
        }

        void assertChromosomeIdenticalToEntity(ChromosomeEntity chromosome) {
            assertNotNull(chromosome);
            assertEquals(entity.getName(), chromosome.getName());
            assertEquals(entity.getGenbank(), chromosome.getGenbank());
            assertEquals(entity.getRefseq(), chromosome.getRefseq());
        }

    }

    @Nested
    class AliasServiceTests {

        private final AssemblyEntity assemblyEntity = AssemblyGenerator.generate();

        private final List<ChromosomeEntity> chromosomeEntities = new LinkedList<>();

        private final int CHROMOSOME_LIST_SIZE = 5;

        @BeforeEach
        void setup() {
            AliasService mockAliasService = mock(AliasService.class);
            for (int i = 0; i < CHROMOSOME_LIST_SIZE; i++) {
                ChromosomeEntity generate = ChromosomeGenerator.generate(i, assemblyEntity);
                chromosomeEntities.add(generate);
                Optional<AssemblyEntity> entityOptional = Optional.of(this.assemblyEntity);
                Mockito.when(mockAliasService.getAssemblyByChromosomeGenbank(generate.getGenbank()))
                       .thenReturn(entityOptional);
                Mockito.when(mockAliasService.getAssemblyByChromosomeRefseq(generate.getRefseq()))
                       .thenReturn(entityOptional);
            }
            Mockito.when(mockAliasService.getChromosomesByAssemblyGenbank(assemblyEntity.getGenbank()))
                   .thenReturn(chromosomeEntities);
            Mockito.when(mockAliasService.getChromosomesByAssemblyRefseq(assemblyEntity.getRefseq()))
                   .thenReturn(chromosomeEntities);
            controller = new ContigAliasController(null, null, mockAliasService, null, null);
        }

        @AfterEach
        void tearDown() {
            chromosomeEntities.clear();
        }

        @Test
        void getAssemblyByChromosomeGenbank() {
            for (ChromosomeEntity chromosomeEntity : chromosomeEntities) {
                ResponseEntity<AssemblyEntity> genbank = controller.getAssemblyByChromosomeGenbank(
                        chromosomeEntity.getGenbank());
                testAssemblyEntityResponse(genbank);
            }
        }

        @Test
        void getAssemblyByChromosomeRefseq() {
            for (ChromosomeEntity chromosomeEntity : chromosomeEntities) {
                testAssemblyEntityResponse(controller.getAssemblyByChromosomeRefseq(chromosomeEntity.getRefseq()));
            }
        }

        @Test
        void getChromosomesByAssemblyGenbank() {
            ResponseEntity<List<ChromosomeEntity>> chromosomes = controller.getChromosomesByAssemblyGenbank(
                    assemblyEntity.getGenbank());
            testChromosomeEntityResponses(chromosomes);
        }

        @Test
        void getChromosomesByAssemblyRefseq() {
            ResponseEntity<List<ChromosomeEntity>> chromosomes = controller.getChromosomesByAssemblyRefseq(
                    assemblyEntity.getRefseq());
            testChromosomeEntityResponses(chromosomes);
        }

        void testAssemblyEntityResponse(ResponseEntity<AssemblyEntity> response) {
            assertEquals(response.getStatusCode(), HttpStatus.OK);
            assertTrue(response.hasBody());
            AssemblyEntity assembly = response.getBody();
            assertNotNull(assembly);
            assertEquals(assemblyEntity.getName(), assembly.getName());
            assertEquals(assemblyEntity.getOrganism(), assembly.getOrganism());
            assertEquals(assemblyEntity.getGenbank(), assembly.getGenbank());
            assertEquals(assemblyEntity.getRefseq(), assembly.getRefseq());
            assertEquals(assemblyEntity.getTaxid(), assembly.getTaxid());
            assertEquals(assemblyEntity.isGenbankRefseqIdentical(), assembly.isGenbankRefseqIdentical());
        }

        void testChromosomeEntityResponses(ResponseEntity<List<ChromosomeEntity>> response) {
            assertEquals(response.getStatusCode(), HttpStatus.OK);
            assertTrue(response.hasBody());
            List<ChromosomeEntity> entities = response.getBody();
            assertNotNull(entities);
            assertEquals(chromosomeEntities.size(), entities.size());
            assertTrue(chromosomeEntities.containsAll(entities));
        }

    }
}
