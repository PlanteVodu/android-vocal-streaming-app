module Vocal
{
	struct Track
	{
		string author;
		string title;
		string filepath;
		int duration;
		string search; // Utilisé pour la recherche sans spécifier les champs author ou title
	};

	sequence<Track> Collection;

	interface Coll
	{
		void add(Track t);
		Collection search(Track t);
		void startStream(Track t);
		Collection searchTrackAndStream(Track t);
		void pauseStream();
		void resumeStream();
		void stopStream();
		Collection getCollection();
	};
};
