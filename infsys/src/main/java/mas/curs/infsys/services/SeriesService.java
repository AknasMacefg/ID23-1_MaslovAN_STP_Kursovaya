package mas.curs.infsys.services;

import mas.curs.infsys.models.Series;
import mas.curs.infsys.repositories.SeriesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class SeriesService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private UserService userService;

    @Autowired
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    private SeriesRepository seriesRepository;

    public List<Series> getAllSeriess()
    {
        return seriesRepository.findAll();
    }

    public Series getSeriesById(long id) {
        return seriesRepository.findById(id).get();
    }

    public void deleteSeries(Long seriesId) {
        seriesRepository.delete(seriesRepository.findById(seriesId).get());
    }

    public boolean addSeries(Series series) {
        if (seriesRepository.existsByName(series.getName().toLowerCase(Locale.ROOT))) {
            return false;
        }
        series.setName(series.getName().toLowerCase(Locale.ROOT));
        seriesRepository.save(series);
        return true;
    }

    public boolean updateSeries(Series series) {
        if (seriesRepository.existsById(series.getId())) {
            if (seriesRepository.existsByName(series.getName().toLowerCase(Locale.ROOT))) {
                return false;
            }
            series.setName(series.getName().toLowerCase(Locale.ROOT));
            seriesRepository.save(series);
            return true;
        }
        return false;
    }


}
