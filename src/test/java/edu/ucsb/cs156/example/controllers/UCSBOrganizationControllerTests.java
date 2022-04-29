package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)
public class UCSBOrganizationControllerTests extends ControllerTestCase {

        @MockBean
        UCSBOrganizationRepository ucsbOrganizationRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/ucsborganization/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsborganization/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/ucsborganization/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/ucsborganization?orgCode=EXC"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/ucsborganization/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsborganization/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/ucsborganization/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // Tests with mocks for database actions
        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                UCSBOrganization org = UCSBOrganization.builder()
                                .orgCode("IND")
                                .orgTranslationShort("Indus")
                                .orgTranslation("UCSBIndus")
                                .inactive(false)
                                .build();

                when(ucsbOrganizationRepository.findById(eq("IND"))).thenReturn(Optional.of(org));

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganization?orgCode=IND"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbOrganizationRepository, times(1)).findById(eq("IND"));
                String expectedJson = mapper.writeValueAsString(org);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }


        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(ucsbOrganizationRepository.findById("NLA")).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganization?orgCode=NLA"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(ucsbOrganizationRepository, times(1)).findById("NLA");
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("UCSBOrganization with id NLA not found", json.get("message"));
        }


        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_ucsborganizations() throws Exception {

                // arrange

                UCSBOrganization nla = UCSBOrganization.builder()
                                .orgCode("NLA")
                                .orgTranslationShort("NirmitLovers")
                                .orgTranslation("NirmitLoversAssociation")
                                .inactive(false)
                                .build();

                UCSBOrganization ccc = UCSBOrganization.builder()
                                .orgCode("CCC")
                                .orgTranslationShort("Costco")
                                .orgTranslation("Costco Club")
                                .inactive(true)
                                .build();

                ArrayList<UCSBOrganization> expectedOrganizations = new ArrayList<>();
                expectedOrganizations.addAll(Arrays.asList(nla, ccc));

                when(ucsbOrganizationRepository.findAll()).thenReturn(expectedOrganizations);

                // act
                MvcResult response = mockMvc.perform(get("/api/ucsborganization/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(ucsbOrganizationRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedOrganizations);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_organization_f() throws Exception {
                // arrange

                UCSBOrganization ceo = UCSBOrganization.builder()
                                .orgCode("CEO")
                                .orgTranslationShort("CookieEaters")
                                .orgTranslation("CookieEatersOrg")
                                .inactive(false)
                                .build();

                when(ucsbOrganizationRepository.save(eq(ceo))).thenReturn(ceo);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/ucsborganization/post?orgCode=CEO&orgTranslationShort=CookieEaters&orgTranslation=CookieEatersOrg&inactive=false")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).save(ceo);
                String expectedJson = mapper.writeValueAsString(ceo);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_organization_t() throws Exception {
                // arrange

                UCSBOrganization ceo = UCSBOrganization.builder()
                                .orgCode("CEO")
                                .orgTranslationShort("CookieEaters")
                                .orgTranslation("CookieEatersOrg")
                                .inactive(true)
                                .build();

                when(ucsbOrganizationRepository.save(eq(ceo))).thenReturn(ceo);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/ucsborganization/post?orgCode=CEO&orgTranslationShort=CookieEaters&orgTranslation=CookieEatersOrg&inactive=true")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).save(ceo);
                String expectedJson = mapper.writeValueAsString(ceo);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_an_organization() throws Exception {
                // arrange

                UCSBOrganization weg = UCSBOrganization.builder()
                            .orgCode("WEG")
                            .orgTranslationShort("WaterEnthusiasts")
                            .orgTranslation("WaterEnthusiastsGroup")
                            .inactive(false)
                            .build();

                when(ucsbOrganizationRepository.findById("WEG")).thenReturn(Optional.of(weg));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsborganization?orgCode=WEG")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById("WEG");
                verify(ucsbOrganizationRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganization with id WEG deleted", json.get("message"));
        }


        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_organization_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(ucsbOrganizationRepository.findById(eq("NEO"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/ucsborganization?orgCode=NEO")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById(eq("NEO"));
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganization with id NEO not found", json.get("message"));
        }
     

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_organization_t2f() throws Exception {
                // arrange

                UCSBOrganization hch1 = UCSBOrganization.builder()
                                .orgCode("HCH")
                                .orgTranslationShort("HotCheetoHaters")
                                .orgTranslation("HotCheetoHatersClub")
                                .inactive(true)
                                .build();

                UCSBOrganization hch2 = UCSBOrganization.builder()
                                .orgCode("HCH")
                                .orgTranslationShort("HOTCHEETOHATERS")
                                .orgTranslation("HOTCHEETOHATERSCLUB")
                                .inactive(false)
                                .build();

                String requestBody = mapper.writeValueAsString(hch2);

                when(ucsbOrganizationRepository.findById(eq("HCH"))).thenReturn(Optional.of(hch1));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsborganization?orgCode=HCH")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById("HCH");
                verify(ucsbOrganizationRepository, times(1)).save(hch2); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_organization_that_does_not_exist() throws Exception {
                // arrange

                UCSBOrganization mpc = UCSBOrganization.builder()
                                .orgCode("MPC")
                                .orgTranslationShort("MexPizza")
                                .orgTranslation("MexicanPizzaClub")
                                .inactive(true)
                                .build();

                String requestBody = mapper.writeValueAsString(mpc);

                when(ucsbOrganizationRepository.findById("MPC")).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/ucsborganization?orgCode=MPC")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(ucsbOrganizationRepository, times(1)).findById("MPC");
                Map<String, Object> json = responseToJson(response);
                assertEquals("UCSBOrganization with id MPC not found", json.get("message"));

        }
}
