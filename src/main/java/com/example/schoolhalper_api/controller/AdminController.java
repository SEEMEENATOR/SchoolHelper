package com.example.schoolhalper_api.controller;

import com.example.schoolhalper_api.domain.Section;
import com.example.schoolhalper_api.domain.TeacherProfile;
import com.example.schoolhalper_api.domain.TeacherProfileViewDTO;
import com.example.schoolhalper_api.domain.User;
import com.example.schoolhalper_api.repository.SectionRepository;
import com.example.schoolhalper_api.repository.TeacherProfileRepository;
import com.example.schoolhalper_api.repository.UserRepository;
import com.example.schoolhalper_api.service.OcrService;
import com.example.schoolhalper_api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.swing.text.html.Option;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Controller
public class AdminController {
    @Autowired
    TeacherProfileRepository teacherProfileRepository;
    @Autowired
    SectionRepository sectionRepository;
    @Autowired
    private OcrService ocrService;
    @Autowired
    UserService userService;
    @Autowired
    UserRepository userRepository;

    @GetMapping("/admin/approve-teachers")
    public String approveTeachersPage(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "sort", required = false, defaultValue = "false") boolean sort,
            Model model) {

        List<TeacherProfileViewDTO> teachers = StreamSupport
                .stream(teacherProfileRepository.findAll().spliterator(), false)
                .map(profile -> {
                    try {
                        return new TeacherProfileViewDTO(profile);
                    } catch (Exception e) {
                        System.err.println("Ошибка при создании DTO для профиля: " + profile.getId() + " — " + e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (search != null && !search.isEmpty()) {
            String lowerSearch = search.toLowerCase();
            String[] searchTerms = lowerSearch.split("\\s+");

            teachers = teachers.stream()
                    .filter(t -> {
                        var user = t.getProfile().getUser();
                        if (user == null) return false;
                        String firstName = user.getFirstName();
                        String lastName = user.getLastName();

                        return Arrays.stream(searchTerms)
                                .anyMatch(term -> (firstName != null && firstName.toLowerCase().contains(term))
                                        || (lastName != null && lastName.toLowerCase().contains(term)));
                    })
                    .collect(Collectors.toList());
        }

        if (sort) {
            teachers.sort(Comparator.comparing(t -> {
                Boolean isVerified = t.getProfile().getIsVerifiedBySystem();
                return isVerified != null ? !isVerified : true; // неподтверждённые в конец
            }));
        }

        model.addAttribute("teachers", teachers);
        model.addAttribute("search", search != null ? search : "");
        model.addAttribute("sort", sort);

        return "approveTeachersPage";
    }



    @GetMapping("/admin/approve-teachers/list")
    public String approveTeachersList(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "sort", required = false, defaultValue = "false") boolean sort,
            Model model) {

        List<TeacherProfileViewDTO> teachers = StreamSupport
                .stream(teacherProfileRepository.findAll().spliterator(), false)
                .map(profile -> {
                    try {
                        return new TeacherProfileViewDTO(profile);
                    } catch (Exception e) {
                        System.err.println("Ошибка при создании DTO для профиля: " + profile.getId() + " — " + e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (search != null && !search.isEmpty()) {
            String lowerSearch = search.toLowerCase();
            teachers = teachers.stream()
                    .filter(t -> {
                        var user = t.getProfile().getUser();
                        if (user == null) return false;
                        String firstName = user.getFirstName();
                        String lastName = user.getLastName();
                        return (firstName != null && firstName.toLowerCase().contains(lowerSearch))
                                || (lastName != null && lastName.toLowerCase().contains(lowerSearch));
                    })
                    .collect(Collectors.toList());
        }

        if (sort) {
            teachers.sort(Comparator.comparing(t -> {
                Boolean isVerified = t.getProfile().getIsVerifiedBySystem();
                return isVerified != null ? !isVerified : true;
            }));
        }

        model.addAttribute("teachers", teachers);
        return "approveTeachersList";
    }



    @PostMapping("/admin/approve-teacher/{id}")
    public String approveTeacher(@PathVariable("id") Long id){
        teacherProfileRepository.findById(id).ifPresent(teacher -> {
        teacher.setIsApproved(true);
        teacherProfileRepository.save(teacher);
    });
    return "redirect:/admin/approve-teachers";
    }
    @PostMapping("/admin/reject-teacher/{id}")
    public String rejectTeacher(@PathVariable("id") Long id){
        teacherProfileRepository.findById(id).ifPresent(teacher -> {
            teacher.setIsApproved(false);
            teacherProfileRepository.save(teacher);
        });
        return "redirect:/admin/approve-teachers";
    }

    @GetMapping("/admin/sections")
    public String sectionsPage(Model model, @RequestParam(required = false) String action) {
        List<Section> sections = StreamSupport
                .stream(sectionRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());

        sections.forEach(section -> {
            if (section.getDescription() == null) {
                section.setDescription("Описание отсутствует");
            }
        });

        model.addAttribute("sections", sections.isEmpty() ? null : sections); // Передаем null, если список пуст
        model.addAttribute("addForm", "add".equals(action));
        return "adminSections";
    }

    @PostMapping("/admin/sections/add")
    public String addSection(@RequestParam String title,
    @RequestParam String description
    ){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.isAuthenticated()){
            String username = authentication.getName();
            Optional<User> adminUser = userRepository.findByUsername(username);
            if(adminUser.isPresent() && adminUser.get().getRole() == User.Role.ADMIN){
                Section section = Section.builder()
                        .title(title)
                        .description(description)
                        .admin(adminUser.get())
                        .build();
                sectionRepository.save(section);
            }
        }
        return "redirect:/admin/sections";
    }
    @PostMapping("/admin/edit-section/{id}")
    public String editSection(@PathVariable Long id,
                              @RequestParam String title,
                              @RequestParam String description) {
        Optional<Section> sectionOpt = sectionRepository.findById(id);
        if (sectionOpt.isPresent()) {
            Section section = sectionOpt.get();
            section.setTitle(title);
            section.setDescription(description != null && !description.trim().isEmpty() ? description : "Описание отсутствует");
            sectionRepository.save(section);
        }
        return "redirect:/admin/sections";
    }

    @PostMapping("/admin/delete-section/{id}")
    public String deleteSection(@PathVariable Long id) {
        sectionRepository.deleteById(id);
        return "redirect:/admin/sections";
    }

    @GetMapping("/admin/blockPage")
    public String blockPage(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "role", required = false) String role,
            Model model) {

        List<User> users = StreamSupport.stream(userRepository.findAll().spliterator(), false)
                .filter(u -> u.getRole() == User.Role.STUDENT || u.getRole() == User.Role.TEACHER)
                .collect(Collectors.toList());

        if (search != null && !search.isEmpty()) {
            String lowerSearch = search.toLowerCase();
            users = users.stream()
                    .filter(u -> u.getUsername().toLowerCase().contains(lowerSearch))
                    .collect(Collectors.toList());
        }

        if (role != null && !role.isEmpty()) {
            users = users.stream()
                    .filter(u -> u.getRole().toString().equalsIgnoreCase(role))
                    .collect(Collectors.toList());

            model.addAttribute("roleIsStudent", "STUDENT".equalsIgnoreCase(role));
            model.addAttribute("roleIsTeacher", "TEACHER".equalsIgnoreCase(role));
        }

        model.addAttribute("users", users);
        model.addAttribute("search", search != null ? search : "");

        return "blockPage";
    }


    @PostMapping("/admin/blockUser")
    public String blockUser(@RequestParam String username) {
        try {
            userService.blockUser(username);
            return "redirect:/admin/blockPage";
        } catch (UsernameNotFoundException e) {
            return "redirect:/admin/blockPage?error=UserNotFound";
        }
    }
    @PostMapping("/admin/unblockUser")
    public String unblockUser(@RequestParam String username) {
        try {
            userService.unblockUser(username);
            return "redirect:/admin/blockPage";
        } catch (UsernameNotFoundException e) {
            return "redirect:/admin/blockPage?error=UserNotFound";
        }
    }

}
