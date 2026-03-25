import axios from "axios";

export const searchCrime = async (query, location) => {

  const res = await axios.get(
    "/api/crime/search",
    {
      params: {
        query,
        location
      }
    }
  );

  return res.data;

};