package com.maths22.ftc.season.vv;

import com.maths22.ftc.entities.Score;
import com.maths22.ftc.season.SeasonManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Jacob on 9/6/2016.
 */
@Component(SeasonManager.BEAN_NAME_PREFIX + VvManager.SEASON_SLUG)
public class VvManager implements SeasonManager<SeasonScoreVv> {
    public static final String SEASON_SLUG = "vv";

    @Override
    public String getSlug() {
        return SEASON_SLUG;
    }

    @Autowired
    SeasonScoreVvRepository scoreRepository;

    @Override
    public SeasonScoreVv createNewScore() {
        return new SeasonScoreVv();
    }

    /* VV score descr
       0   autoBeacons
       1   autoLargeBall
       2   autoSmallBallCenterVortex
       3   autoSmallBallCornerVortex
       4   autoRobot1Location
       5   autoRobot2Location
       6   teleBeacons
       7   teleSmallBallCenterVortex
       8   teleSmallBallCornerVortex
       9   endgLargeBallLocation
       10  minorPenalties
       11  majorPenalties
     */

    @Override
    public SeasonScoreVv convertLegacyArray(Score baseScore, String[] line, int offset) {
        if(!(baseScore instanceof SeasonScoreVv)) {
            return null;
        }
        SeasonScoreVv score = (SeasonScoreVv) baseScore;
        score.setAutoBeacons(Integer.parseInt(line[offset + 0]));
        score.setAutoCapFloor(Boolean.parseBoolean(line[offset + 1]));
        score.setAutoCenterParticles(Integer.parseInt(line[offset + 2]));
        score.setAutoCornerParticles(Integer.parseInt(line[offset + 3]));
        score.setR1AutoPos(ordToRobotPos(Integer.parseInt(line[offset+4])));
        score.setR2AutoPos(ordToRobotPos(Integer.parseInt(line[offset+5])));
        score.setDriverBeacons(Integer.parseInt(line[offset + 6]));
        score.setDriverCenterParticles(Integer.parseInt(line[offset + 7]));
        score.setDriverCornerParticles(Integer.parseInt(line[offset + 8]));
        score.setDriverCapPosition(ordToCapPos(Integer.parseInt(line[offset+9])));
        score.setMinorPenalties(Integer.parseInt(line[offset+10]));
        score.setMajorPenalties(Integer.parseInt(line[offset+11]));
        return score;
    }

    private RobotPosition ordToRobotPos(int ord) {
        switch(ord) {
            case 0:
                return RobotPosition.FLOOR;
            case 1:
                return RobotPosition.PARTIAL_CENTER;
            case 2:
                return RobotPosition.FULL_CENTER;
            case 3:
                return RobotPosition.PARTIAL_CORNER;
            case 4:
                return RobotPosition.FULL_CORNER;
            default:
                return null;
        }
    }

    private CapPosition ordToCapPos(int ord) {
        switch(ord) {
            case 0:
                return CapPosition.FLOOR;
            case 1:
                return CapPosition.LOW;
            case 2:
                return CapPosition.HIGH;
            case 3:
                return CapPosition.VORTEX;
            default:
                return null;
        }
    }


    @Override
    public SeasonScoreVvRepository getScoreRepository() {
        return scoreRepository;
    }


}
