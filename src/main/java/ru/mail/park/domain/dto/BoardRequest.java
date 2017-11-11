package ru.mail.park.domain.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;
import ru.mail.park.info.constants.MessageConstants;
import ru.mail.park.mechanics.objects.body.GBody;
import ru.mail.park.mechanics.objects.joint.GJoint;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

public class BoardRequest {
    @NotNull(message = MessageConstants.BOARD_META_EMPTY)
    private BoardMetaDto boardMetaDto;
    @NotNull(message = MessageConstants.BOARD_DATA_EMPTY)
    @Valid
    private Data boardData;

    @JsonCreator
    public BoardRequest() {

    }

    @JsonProperty("meta")
    public BoardMetaDto getBoardMetaDto() {
        return boardMetaDto;
    }

    public void setBoardMetaDto(BoardMetaDto boardMetaDto) {
        this.boardMetaDto = boardMetaDto;
    }

    @JsonProperty("data")
    public Data getBoardData() {
        return boardData;
    }

    public void setBoardData(Data boardData) {
        this.boardData = boardData;
    }

    public static class Data {
        @NotEmpty(message = MessageConstants.BODIES_LIST_EMPTY)
        @Valid
        private List<GBody> bodies;
        @Valid
        private List<GJoint> joints;

        @JsonCreator
        public Data() {

        }

        public List<GBody> getBodies() {
            return bodies;
        }

        public void setBodies(List<GBody> bodies) {
            this.bodies = bodies;
        }

        public List<GJoint> getJoints() {
            return joints;
        }

        public void setJoints(List<GJoint> joints) {
            this.joints = joints;
        }
    }
}
