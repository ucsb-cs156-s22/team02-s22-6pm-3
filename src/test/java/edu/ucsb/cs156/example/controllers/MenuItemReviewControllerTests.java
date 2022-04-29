package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.controllers.MenuItemReviewController;
import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;

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

import java.time.LocalDateTime;

@WebMvcTest(controllers = MenuItemReviewController.class)
@Import(TestConfig.class)
public class MenuItemReviewControllerTests extends ControllerTestCase {

        @MockBean
        MenuItemReviewRepository menuItemReviewRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/MenuItemReview/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/MenuItemReview/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/MenuItemReview/all"))
                                .andExpect(status().is(200)); // logged
        }

        /* Tests GET one review if user not logged in, should throw error */
        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/MenuItemReview?id=1"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/ucsbdiningcommons/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/MenuItemReview/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/MenuItemReview/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // Tests with mocks for database actions

        /* tests GET one review that succeeds */
        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                LocalDateTime ldt = LocalDateTime.parse("2022-04-28T14:35:00");
                MenuItemReview pizzaReview1 = MenuItemReview.builder()
                                .itemId(1)
                                .reviewerEmail("yl@ucsb.edu")
                                .stars(2)
                                .dateReviewed(ldt)
                                .comments("pizzaReview1")
                                .build();

                when(menuItemReviewRepository.findById(eq(1L))).thenReturn(Optional.of(pizzaReview1));

                // act
                MvcResult response = mockMvc.perform(get("/api/MenuItemReview?id=1"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(menuItemReviewRepository, times(1)).findById(eq(1L));
                String expectedJson = mapper.writeValueAsString(pizzaReview1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }


        /* Tests GET one review but throws error since review not found */
        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(menuItemReviewRepository.findById(eq(1L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/MenuItemReview?id=1"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(menuItemReviewRepository, times(1)).findById(eq(1L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("MenuItemReview with id 1 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_menuitemreview() throws Exception {

                // arrange
                LocalDateTime ldt = LocalDateTime.parse("2022-04-28T14:35:00");
                MenuItemReview pizzaReview1 = MenuItemReview.builder()
                                .itemId(1)
                                .reviewerEmail("yl@ucsb.edu")
                                .stars(2)
                                .dateReviewed(ldt)
                                .comments("pizzaReview1")
                                .build();


                MenuItemReview pizzaReview2 = MenuItemReview.builder()
                                .itemId(1)
                                .reviewerEmail("yl@ucsb.edu")
                                .stars(4)
                                .dateReviewed(ldt)
                                .comments("pizzaReview2")
                                .build();

                ArrayList<MenuItemReview> expectedReviews = new ArrayList<>();
                expectedReviews.addAll(Arrays.asList(pizzaReview1, pizzaReview2));

                when(menuItemReviewRepository.findAll()).thenReturn(expectedReviews);

                // act
                MvcResult response = mockMvc.perform(get("/api/MenuItemReview/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(menuItemReviewRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedReviews);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // test for POST 
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_review() throws Exception {
                // arrange

                LocalDateTime ldt = LocalDateTime.parse("2022-04-28T14:35:00");
                MenuItemReview pizzaReview1 = MenuItemReview.builder()
                                .itemId(1)
                                .reviewerEmail("yl@ucsb.edu")
                                .stars(2)
                                .dateReviewed(ldt)
                                .comments("pizzaReview1")
                                .build();

                when(menuItemReviewRepository.save(eq(pizzaReview1))).thenReturn(pizzaReview1);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/MenuItemReview/post?itemId=1&reviewerEmail=yl@ucsb.edu&stars=2&dateReviewed=2022-04-28T14:35:00&comments=pizzaReview1")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(menuItemReviewRepository, times(1)).save(pizzaReview1);
                String expectedJson = mapper.writeValueAsString(pizzaReview1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        // @WithMockUser(roles = { "ADMIN", "USER" })
        // @Test
        // public void admin_can_delete_a_date() throws Exception {
        //         // arrange

        //         UCSBDiningCommons portola = UCSBDiningCommons.builder()
        //                         .name("Portola")
        //                         .code("portola")
        //                         .hasSackMeal(true)
        //                         .hasTakeOutMeal(true)
        //                         .hasDiningCam(true)
        //                         .latitude(34.417723)
        //                         .longitude(-119.867427)
        //                         .build();

        //         when(ucsbDiningCommonsRepository.findById(eq("portola"))).thenReturn(Optional.of(portola));

        //         // act
        //         MvcResult response = mockMvc.perform(
        //                         delete("/api/ucsbdiningcommons?code=portola")
        //                                         .with(csrf()))
        //                         .andExpect(status().isOk()).andReturn();

        //         // assert
        //         verify(ucsbDiningCommonsRepository, times(1)).findById("portola");
        //         verify(ucsbDiningCommonsRepository, times(1)).delete(any());

        //         Map<String, Object> json = responseToJson(response);
        //         assertEquals("UCSBDiningCommons with id portola deleted", json.get("message"));
        // }

        // @WithMockUser(roles = { "ADMIN", "USER" })
        // @Test
        // public void admin_tries_to_delete_non_existant_commons_and_gets_right_error_message()
        //                 throws Exception {
        //         // arrange

        //         when(ucsbDiningCommonsRepository.findById(eq("munger-hall"))).thenReturn(Optional.empty());

        //         // act
        //         MvcResult response = mockMvc.perform(
        //                         delete("/api/ucsbdiningcommons?code=munger-hall")
        //                                         .with(csrf()))
        //                         .andExpect(status().isNotFound()).andReturn();

        //         // assert
        //         verify(ucsbDiningCommonsRepository, times(1)).findById("munger-hall");
        //         Map<String, Object> json = responseToJson(response);
        //         assertEquals("UCSBDiningCommons with id munger-hall not found", json.get("message"));
        // }

        // @WithMockUser(roles = { "ADMIN", "USER" })
        // @Test
        // public void admin_can_edit_an_existing_commons() throws Exception {
        //         // arrange

        //         UCSBDiningCommons carrilloOrig = UCSBDiningCommons.builder()
        //                         .name("Carrillo")
        //                         .code("carrillo")
        //                         .hasSackMeal(false)
        //                         .hasTakeOutMeal(false)
        //                         .hasDiningCam(true)
        //                         .latitude(34.409953)
        //                         .longitude(-119.85277)
        //                         .build();

        //         UCSBDiningCommons carrilloEdited = UCSBDiningCommons.builder()
        //                         .name("Carrillo Dining Hall")
        //                         .code("carrillo")
        //                         .hasSackMeal(true)
        //                         .hasTakeOutMeal(true)
        //                         .hasDiningCam(false)
        //                         .latitude(34.409954)
        //                         .longitude(-119.85278)
        //                         .build();

        //         String requestBody = mapper.writeValueAsString(carrilloEdited);

        //         when(ucsbDiningCommonsRepository.findById(eq("carrillo"))).thenReturn(Optional.of(carrilloOrig));

        //         // act
        //         MvcResult response = mockMvc.perform(
        //                         put("/api/ucsbdiningcommons?code=carrillo")
        //                                         .contentType(MediaType.APPLICATION_JSON)
        //                                         .characterEncoding("utf-8")
        //                                         .content(requestBody)
        //                                         .with(csrf()))
        //                         .andExpect(status().isOk()).andReturn();

        //         // assert
        //         verify(ucsbDiningCommonsRepository, times(1)).findById("carrillo");
        //         verify(ucsbDiningCommonsRepository, times(1)).save(carrilloEdited); // should be saved with updated info
        //         String responseString = response.getResponse().getContentAsString();
        //         assertEquals(requestBody, responseString);
        // }

        // @WithMockUser(roles = { "ADMIN", "USER" })
        // @Test
        // public void admin_cannot_edit_commons_that_does_not_exist() throws Exception {
        //         // arrange

        //         UCSBDiningCommons editedCommons = UCSBDiningCommons.builder()
        //                         .name("Munger Hall")
        //                         .code("munger-hall")
        //                         .hasSackMeal(false)
        //                         .hasTakeOutMeal(false)
        //                         .hasDiningCam(true)
        //                         .latitude(34.420799)
        //                         .longitude(-119.852617)
        //                         .build();

        //         String requestBody = mapper.writeValueAsString(editedCommons);

        //         when(ucsbDiningCommonsRepository.findById(eq("munger-hall"))).thenReturn(Optional.empty());

        //         // act
        //         MvcResult response = mockMvc.perform(
        //                         put("/api/ucsbdiningcommons?code=munger-hall")
        //                                         .contentType(MediaType.APPLICATION_JSON)
        //                                         .characterEncoding("utf-8")
        //                                         .content(requestBody)
        //                                         .with(csrf()))
        //                         .andExpect(status().isNotFound()).andReturn();

        //         // assert
        //         verify(ucsbDiningCommonsRepository, times(1)).findById("munger-hall");
        //         Map<String, Object> json = responseToJson(response);
        //         assertEquals("UCSBDiningCommons with id munger-hall not found", json.get("message"));

        // }
}
