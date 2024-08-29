package org.sopt.makers.crew.main.entity.comment;

import java.util.List;

import org.sopt.makers.crew.main.common.util.MentionSecretStringRemover;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Comments {
	private final List<Comment> comments;

	public void deleteMention(String mentionName, String mentionOrgId) {
		comments.forEach(comment -> {
			String deletedMentionContent = MentionSecretStringRemover.deleteMentionContent(comment.getContents(),
				mentionName, mentionOrgId);
			comment.updateContents(deletedMentionContent);
		});
	}

	public boolean hasChild() {
		return !comments.isEmpty();
	}
}
