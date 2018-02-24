package ru.mail.park.domain.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;
import ru.mail.park.info.constants.MessageConstants;
import ru.mail.park.mechanics.domain.objects.body.GBody;
import ru.mail.park.mechanics.domain.objects.joint.GJoint;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@SuppressWarnings("RedundantNoArgConstructor")
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

    @SuppressWarnings("unused")
    public void setBoardMetaDto(BoardMetaDto boardMetaDto) {
        this.boardMetaDto = boardMetaDto;
    }

    @JsonProperty("data")
    public Data getBoardData() {
        return boardData;
    }

    @SuppressWarnings("unused")
    public void setBoardData(Data boardData) {
        this.boardData = boardData;
    }

    @SuppressWarnings("RedundantNoArgConstructor")
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

        @SuppressWarnings("unused")
        public void setBodies(List<GBody> bodies) {
            this.bodies = bodies;
        }

        public List<GJoint> getJoints() {
            return joints;
        }

        @SuppressWarnings("unused")
        public void setJoints(List<GJoint> joints) {
            this.joints = joints;
        }
    }
}
