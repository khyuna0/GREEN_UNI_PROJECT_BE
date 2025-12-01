package com.green.university.repository.model;

import lombok.Data;

import java.awt.*;

@Data
public class Evaluation {

		private Long evaluationId;
		private Long studentId;
		private Long subjectId;
		private Long gna1;
		private Long gna2;
		private Long gna3;
		private Long gna4;
		private Long gna5;
		private Long gna6;
		private Long gna7;
		private TextArea improvements;

}
