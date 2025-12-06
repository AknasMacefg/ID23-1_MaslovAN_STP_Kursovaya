package mas.curs.infsys.controllers;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            
            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("errorCode", "404");
                model.addAttribute("errorMessage", "Страница не найдена");
                model.addAttribute("errorDescription", "Запрашиваемая страница не существует.");
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                model.addAttribute("errorCode", "403");
                model.addAttribute("errorMessage", "Доступ запрещен");
                model.addAttribute("errorDescription", "У вас нет прав для доступа к этому ресурсу.");
            } else if (statusCode == HttpStatus.METHOD_NOT_ALLOWED.value()) {
                model.addAttribute("errorCode", "405");
                model.addAttribute("errorMessage", "Метод не разрешен");
                model.addAttribute("errorDescription", "Используемый HTTP-метод не поддерживается для этого ресурса.");
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                model.addAttribute("errorCode", "500");
                model.addAttribute("errorMessage", "Внутренняя ошибка сервера");
                model.addAttribute("errorDescription", "Произошла внутренняя ошибка сервера. Пожалуйста, попробуйте позже.");
            } else {
                model.addAttribute("errorCode", statusCode.toString());
                model.addAttribute("errorMessage", "Ошибка");
                model.addAttribute("errorDescription", "Произошла ошибка при обработке запроса.");
            }
        } else {
            model.addAttribute("errorCode", "Ошибка");
            model.addAttribute("errorMessage", "Неизвестная ошибка");
            model.addAttribute("errorDescription", "Произошла неизвестная ошибка.");
        }
        
        return "error";
    }
}

